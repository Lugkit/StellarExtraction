package com.lugkit.stellarextraction

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lugkit.stellarextraction.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.min

data class GameState(
    val planetResources: Map<String, Double> = RESOURCES.associate { it.id to 0.0 },
    val stationResources: Map<String, Double> = RESOURCES.associate { it.id to 0.0 },
    val unlockedResources: Set<String> = setOf("iron"),
    val purchasedUpgrades: Set<String> = emptySet(),
    val builtModules: Set<String> = emptySet(),
    val activeShipments: List<Shipment> = emptyList(),
    val autoTransportEnabled: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("stellar_save", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    init {
        load()
        startGameLoop()
    }

    // ─── Rates ───────────────────────────────────────────────────────────────

    fun miningRate(resourceId: String): Double {
        val base = RESOURCE_MAP[resourceId]?.baseRate ?: return 0.0
        var multiplier = 1.0
        val s = _state.value
        for (upId in s.purchasedUpgrades) {
            val up = UPGRADES.find { it.id == upId } ?: continue
            if (up.category == "mining") {
                if (up.targetResource == null || up.targetResource == resourceId) {
                    multiplier *= up.multiplier
                }
            }
            if (up.category == "research") multiplier *= up.multiplier
        }
        if (s.builtModules.contains("habitat")) multiplier *= 1.1
        return base * multiplier
    }

    fun planetCap(resourceId: String): Double {
        val base = RESOURCE_MAP[resourceId]?.planetCap ?: return 0.0
        return base
    }

    fun stationCap(resourceId: String): Double {
        val base = RESOURCE_MAP[resourceId]?.stationCap ?: return 0.0
        var mult = 1.0
        val s = _state.value
        if (s.purchasedUpgrades.contains("storage_bay")) mult *= 2.0
        if (s.purchasedUpgrades.contains("storage_mk2")) mult *= 3.0
        return base * mult
    }

    fun travelSeconds(): Double {
        var secs = 30.0
        val s = _state.value
        if (s.purchasedUpgrades.contains("ion_drive")) secs *= 0.7
        if (s.purchasedUpgrades.contains("warp_hop")) secs *= 0.5
        if (s.purchasedUpgrades.contains("quantum_tunnel") && s.builtModules.contains("warp_core")) secs = 0.5
        return secs
    }

    fun launchCapacity(resourceId: String): Double {
        val cap = planetCap(resourceId) * 0.25
        var mult = 1.0
        val s = _state.value
        if (s.purchasedUpgrades.contains("cargo_pod")) mult *= 1.5
        if (s.purchasedUpgrades.contains("cargo_mk2")) mult *= 2.0
        return cap * mult
    }

    // ─── Game loop ───────────────────────────────────────────────────────────

    private fun startGameLoop() {
        viewModelScope.launch {
            var lastSaveTime = System.currentTimeMillis()
            var autoTransportTimer = 0L
            while (true) {
                delay(100)
                val now = System.currentTimeMillis()
                val dt = 0.1

                _state.value = _state.value.let { s ->
                    val newPlanet = s.planetResources.toMutableMap()
                    val newStation = s.stationResources.toMutableMap()
                    val newUnlocked = s.unlockedResources.toMutableSet()

                    // Mine
                    for (res in RESOURCES) {
                        if (res.id !in s.unlockedResources) continue
                        val cap = planetCap(res.id)
                        val current = newPlanet[res.id] ?: 0.0
                        newPlanet[res.id] = min(current + miningRate(res.id) * dt, cap)
                    }

                    // Check unlock conditions
                    for (res in RESOURCES) {
                        if (res.id in newUnlocked) continue
                        if (res.unlockCost.all { (k, v) -> (newStation[k] ?: 0.0) >= v }) {
                            newUnlocked.add(res.id)
                        }
                    }

                    // Arrive shipments
                    val arrived = mutableListOf<Shipment>()
                    val remaining = mutableListOf<Shipment>()
                    for (ship in s.activeShipments) {
                        val elapsed = (now - ship.startTime) / 1000.0
                        if (elapsed >= ship.travelSeconds) {
                            arrived.add(ship)
                        } else {
                            remaining.add(ship)
                        }
                    }
                    for (ship in arrived) {
                        val cap = stationCap(ship.resourceId)
                        val cur = newStation[ship.resourceId] ?: 0.0
                        newStation[ship.resourceId] = min(cur + ship.amount, cap)
                    }

                    s.copy(
                        planetResources = newPlanet,
                        stationResources = newStation,
                        unlockedResources = newUnlocked,
                        activeShipments = remaining
                    )
                }

                // Auto-transport
                if (_state.value.purchasedUpgrades.contains("auto_transport")) {
                    autoTransportTimer += 100
                    if (autoTransportTimer >= 60_000) {
                        autoTransportTimer = 0
                        launchAll()
                    }
                }

                // Autosave every 30s
                if (now - lastSaveTime > 30_000) {
                    save()
                    lastSaveTime = now
                }
            }
        }
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    fun launch(resourceId: String) {
        val s = _state.value
        if (!s.builtModules.contains("docking_bay")) return
        val available = s.planetResources[resourceId] ?: 0.0
        val amount = min(available, launchCapacity(resourceId))
        if (amount <= 0) return

        val ship = Shipment(
            id = System.currentTimeMillis(),
            resourceId = resourceId,
            amount = amount,
            travelSeconds = travelSeconds(),
            startTime = System.currentTimeMillis()
        )
        _state.value = s.copy(
            planetResources = s.planetResources.toMutableMap().also { it[resourceId] = available - amount },
            activeShipments = s.activeShipments + ship
        )
    }

    fun launchAll() {
        for (res in _state.value.unlockedResources.toList()) {
            launch(res)
        }
    }

    fun canBuyUpgrade(id: String): Boolean {
        val up = UPGRADES.find { it.id == id } ?: return false
        val s = _state.value
        if (id in s.purchasedUpgrades) return false
        if (up.requires != null && up.requires !in s.purchasedUpgrades) return false
        if (up.category == "research" && "research_lab" !in s.builtModules) return false
        return up.cost.all { (k, v) -> (s.stationResources[k] ?: 0.0) >= v }
    }

    fun buyUpgrade(id: String) {
        if (!canBuyUpgrade(id)) return
        val up = UPGRADES.find { it.id == id } ?: return
        val s = _state.value
        val newStation = s.stationResources.toMutableMap()
        for ((k, v) in up.cost) newStation[k] = (newStation[k] ?: 0.0) - v
        _state.value = s.copy(
            stationResources = newStation,
            purchasedUpgrades = s.purchasedUpgrades + id
        )
        save()
    }

    fun canBuildModule(id: String): Boolean {
        val mod = MODULE_MAP[id] ?: return false
        val s = _state.value
        if (id in s.builtModules) return false
        if (mod.requires != null && mod.requires !in s.builtModules) return false
        return mod.cost.all { (k, v) -> (s.stationResources[k] ?: 0.0) >= v }
    }

    fun buildModule(id: String) {
        if (!canBuildModule(id)) return
        val mod = MODULE_MAP[id] ?: return
        val s = _state.value
        val newStation = s.stationResources.toMutableMap()
        for ((k, v) in mod.cost) newStation[k] = (newStation[k] ?: 0.0) - v
        _state.value = s.copy(
            stationResources = newStation,
            builtModules = s.builtModules + id
        )
        save()
    }

    // ─── Save / Load ─────────────────────────────────────────────────────────

    fun save() {
        val s = _state.value
        val json = JSONObject().apply {
            put("planet", JSONObject(s.planetResources))
            put("station", JSONObject(s.stationResources))
            put("unlocked", JSONArray(s.unlockedResources.toList()))
            put("upgrades", JSONArray(s.purchasedUpgrades.toList()))
            put("modules", JSONArray(s.builtModules.toList()))
            put("saveTime", System.currentTimeMillis())
        }
        prefs.edit().putString("save", json.toString()).apply()
    }

    private fun load() {
        val raw = prefs.getString("save", null) ?: return
        try {
            val json = JSONObject(raw)
            val saveTime = json.optLong("saveTime", System.currentTimeMillis())
            val offlineSecs = (System.currentTimeMillis() - saveTime) / 1000.0

            val planet = mutableMapOf<String, Double>()
            val planetJson = json.optJSONObject("planet")
            if (planetJson != null) {
                for (key in planetJson.keys()) planet[key] = planetJson.getDouble(key)
            }

            val station = mutableMapOf<String, Double>()
            val stationJson = json.optJSONObject("station")
            if (stationJson != null) {
                for (key in stationJson.keys()) station[key] = stationJson.getDouble(key)
            }

            val unlocked = mutableSetOf<String>()
            val unlockedArr = json.optJSONArray("unlocked")
            if (unlockedArr != null) {
                for (i in 0 until unlockedArr.length()) unlocked.add(unlockedArr.getString(i))
            }

            val upgrades = mutableSetOf<String>()
            val upgradesArr = json.optJSONArray("upgrades")
            if (upgradesArr != null) {
                for (i in 0 until upgradesArr.length()) upgrades.add(upgradesArr.getString(i))
            }

            val modules = mutableSetOf<String>()
            val modulesArr = json.optJSONArray("modules")
            if (modulesArr != null) {
                for (i in 0 until modulesArr.length()) modules.add(modulesArr.getString(i))
            }

            // Offline progress (cap at 8 hours)
            val offlineCapped = minOf(offlineSecs, 8 * 3600.0)
            val tempState = GameState(planet, station, unlocked, upgrades, modules)
            _state.value = tempState
            for (res in RESOURCES) {
                if (res.id !in unlocked) continue
                val rate = miningRate(res.id)
                val cap = planetCap(res.id)
                val cur = planet[res.id] ?: 0.0
                planet[res.id] = min(cur + rate * offlineCapped, cap)
            }

            _state.value = GameState(planet, station, unlocked, upgrades, modules)
        } catch (_: Exception) {}
    }
}
