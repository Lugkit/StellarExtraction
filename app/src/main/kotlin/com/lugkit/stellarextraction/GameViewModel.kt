package com.lugkit.stellarextraction

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

enum class Resource { IRON, QUARTZ, ENERGY, TITANIUM, IRIDIUM, XENON }

data class BuildCost(
    val iron: Double = 0.0,
    val quartz: Double = 0.0,
    val titanium: Double = 0.0,
    val energy: Double = 0.0,
    val iridium: Double = 0.0,
    val xenon: Double = 0.0
)

data class GameState(
    val iron: Double = 0.0,
    val quartz: Double = 0.0,
    val titanium: Double = 0.0,
    val iridium: Double = 0.0,
    val xenon: Double = 0.0,
    val energy: Double = 0.0,
    val stellarShards: Int = 0,

    val drillHeadLevel: Int = 0,
    val powerCoreLevel: Int = 0,
    val solarArrayLevel: Int = 0,       // 0=none, 1=lv1, 2=lv2
    val deepShaftLevel: Int = 0,
    val hasLaunchSilo: Boolean = false,
    val hasRelaySatellite: Boolean = false,
    val hasOrbitalLab: Boolean = false,
    val hasAsteroidMiner: Boolean = false,
    val hasOrbitalSolarStation: Boolean = false,
    val hasRefinery: Boolean = false,
    val hasCoreTap: Boolean = false,
    val hasPlanetCore: Boolean = false,
    val buildingSeed: Long = 12345L
) {
    val ironPerSec: Double get() = when (drillHeadLevel) {
        1 -> 1.0; 2 -> 3.0; 3 -> 9.0; 4 -> 27.0; else -> 0.0
    }
    // Quartz: drill produces 0.5/s, Power Core consumes 1/s upkeep
    val quartzBasePerSec: Double  get() = if (drillHeadLevel >= 2) 1.5 else 0.0
    val quartzUpkeep: Double      get() = if (powerCoreLevel >= 1) 1.0 else 0.0
    val quartzPerSec: Double      get() = quartzBasePerSec - quartzUpkeep

    val solarPerSec: Double get() = solarArrayLevel.toDouble()          // lv1=1, lv2=2
    val energyPerSec: Double get() =
        (if (powerCoreLevel >= 1) 3.0 else 0.0) +
        solarPerSec +
        (if (hasOrbitalSolarStation) 20.0 else 0.0)

    val titaniumPerSec: Double get() = if (deepShaftLevel >= 1) 0.5  else 0.0
    val iridiumPerSec: Double  get() = if (deepShaftLevel >= 2) 0.15 else 0.0
    val xenonPerSec: Double    get() = if (hasAsteroidMiner)    0.05 else 0.0

    val quartzVisible: Boolean         get() = drillHeadLevel >= 2
    val energyVisible: Boolean         get() = powerCoreLevel >= 1 || solarArrayLevel >= 1
    val titaniumVisible: Boolean       get() = deepShaftLevel >= 1
    val iridiumVisible: Boolean        get() = deepShaftLevel >= 2
    val xenonVisible: Boolean          get() = hasAsteroidMiner
    val stellarShardsVisible: Boolean  get() = stellarShards > 0

    fun canAfford(cost: BuildCost): Boolean =
        iron >= cost.iron && quartz >= cost.quartz && titanium >= cost.titanium &&
        energy >= cost.energy && iridium >= cost.iridium && xenon >= cost.xenon

    fun spend(cost: BuildCost): GameState = copy(
        iron     = iron     - cost.iron,
        quartz   = quartz   - cost.quartz,
        titanium = titanium - cost.titanium,
        energy   = energy   - cost.energy,
        iridium  = iridium  - cost.iridium,
        xenon    = xenon    - cost.xenon
    )

    fun applyProduction(seconds: Double): GameState = copy(
        iron     = iron     + ironPerSec     * seconds,
        quartz   = (quartz  + quartzPerSec   * seconds).coerceAtLeast(0.0),
        energy   = energy   + energyPerSec   * seconds,
        titanium = titanium + titaniumPerSec * seconds,
        iridium  = iridium  + iridiumPerSec  * seconds,
        xenon    = xenon    + xenonPerSec    * seconds
    )
}

// ── Costs ─────────────────────────────────────────────────────────────────────

val drillHeadCosts = mapOf(
    1 to BuildCost(iron =     10.0),
    2 to BuildCost(iron =    400.0),
    3 to BuildCost(iron =  5_000.0, energy   =    80.0),
    4 to BuildCost(iron = 28_000.0, titanium = 1_000.0)
)
val deepShaftCosts = mapOf(
    1 to BuildCost(iron =  8_000.0),
    2 to BuildCost(iron = 40_000.0, titanium = 1_000.0)
)
val powerCoreCost          = BuildCost(iron =  1_500.0, quartz   =   200.0)
val solarArrayCosts        = mapOf(
    1 to BuildCost(quartz =   800.0),
    2 to BuildCost(quartz = 2_000.0)
)
val launchSiloCostA        = BuildCost(iron = 28_000.0, titanium =   500.0)  // Path A: titanium-heavy
val launchSiloCostB        = BuildCost(iron = 15_000.0, quartz   = 2_000.0)  // Path B: quartz-slow
val relaySatelliteCost     = BuildCost(iron = 80_000.0, titanium = 2_000.0)
val orbitalLabCost         = BuildCost(iridium =  2_000.0)
val asteroidMinerCost      = BuildCost(iridium = 10_000.0)
val orbitalSolarStationCost= BuildCost(iridium =  3_000.0)
val refineryCost           = BuildCost(iron =  3_000.0)
val coreTapCost            = BuildCost(xenon =    500.0)
val planetCoreCost         = BuildCost(xenon =  2_000.0)

// Refinery conversion: spend 1 of higher resource, gain downward
val refineryRates = mapOf(
    Resource.IRIDIUM  to Pair(BuildCost(iridium  = 1.0), BuildCost(titanium = 3.0)),
    Resource.TITANIUM to Pair(BuildCost(titanium = 1.0), BuildCost(quartz   = 5.0)),
    Resource.QUARTZ   to Pair(BuildCost(quartz   = 1.0), BuildCost(iron     = 5.0))
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("stellar_game", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        loadAndApplyOfflineProgress()
        viewModelScope.launch {
            var tick = 0
            while (true) {
                delay(1000)
                _state.value = _state.value.applyProduction(1.0)
                if (++tick % 30 == 0) saveState()
            }
        }
    }

    fun saveState() {
        try {
            val s = _state.value
            val json = JSONObject().apply {
                put("iron",                    s.iron)
                put("quartz",                  s.quartz)
                put("titanium",                s.titanium)
                put("iridium",                 s.iridium)
                put("xenon",                   s.xenon)
                put("energy",                  s.energy)
                put("stellarShards",           s.stellarShards)
                put("drillHeadLevel",          s.drillHeadLevel)
                put("powerCoreLevel",          s.powerCoreLevel)
                put("solarArrayLevel",         s.solarArrayLevel)
                put("deepShaftLevel",          s.deepShaftLevel)
                put("hasLaunchSilo",           s.hasLaunchSilo)
                put("hasRelaySatellite",       s.hasRelaySatellite)
                put("hasOrbitalLab",           s.hasOrbitalLab)
                put("hasAsteroidMiner",        s.hasAsteroidMiner)
                put("hasOrbitalSolarStation",  s.hasOrbitalSolarStation)
                put("hasRefinery",             s.hasRefinery)
                put("hasCoreTap",              s.hasCoreTap)
                put("hasPlanetCore",           s.hasPlanetCore)
                put("buildingSeed",            s.buildingSeed)
                put("savedAt",                 System.currentTimeMillis())
            }.toString()
            prefs.edit().putString("game_state", json).apply()
        } catch (_: Exception) {}
    }

    private fun loadAndApplyOfflineProgress() {
        try {
            val raw = prefs.getString("game_state", null) ?: return
            val obj = JSONObject(raw)
            val savedAt = obj.getLong("savedAt")
            val elapsed = min((System.currentTimeMillis() - savedAt) / 1000.0, 8.0 * 3600)
            val s = GameState(
                iron                   = obj.getDouble("iron"),
                quartz                 = obj.getDouble("quartz"),
                titanium               = obj.getDouble("titanium"),
                iridium                = obj.getDouble("iridium"),
                xenon                  = obj.getDouble("xenon"),
                energy                 = obj.getDouble("energy"),
                stellarShards          = obj.getInt("stellarShards"),
                drillHeadLevel         = obj.getInt("drillHeadLevel"),
                powerCoreLevel         = obj.getInt("powerCoreLevel"),
                solarArrayLevel        = obj.optInt("solarArrayLevel", 0),
                deepShaftLevel         = obj.getInt("deepShaftLevel"),
                hasLaunchSilo          = obj.getBoolean("hasLaunchSilo"),
                hasRelaySatellite      = obj.getBoolean("hasRelaySatellite"),
                hasOrbitalLab          = obj.getBoolean("hasOrbitalLab"),
                hasAsteroidMiner       = obj.getBoolean("hasAsteroidMiner"),
                hasOrbitalSolarStation = obj.optBoolean("hasOrbitalSolarStation", false),
                hasRefinery            = obj.optBoolean("hasRefinery", false),
                hasCoreTap             = obj.getBoolean("hasCoreTap"),
                hasPlanetCore          = obj.getBoolean("hasPlanetCore"),
                buildingSeed           = obj.optLong("buildingSeed", 12345L)
            )
            _state.value = if (elapsed > 1) s.applyProduction(elapsed) else s
        } catch (_: Exception) {}
    }

    // Spread mining: 0.5s of all resources
    fun strike() {
        _state.value = _state.value.let { s ->
            s.copy(
                iron     = s.iron     + max(1.0, s.ironPerSec)   * 0.5,
                quartz   = (s.quartz  + s.quartzPerSec           * 0.5).coerceAtLeast(0.0),
                energy   = s.energy   + s.energyPerSec           * 0.5,
                titanium = s.titanium + s.titaniumPerSec         * 0.5,
                iridium  = s.iridium  + s.iridiumPerSec          * 0.5,
                xenon    = s.xenon    + s.xenonPerSec            * 0.5
            )
        }
    }

    // Focused mining: 1.5s of one resource
    fun focusedStrike(resource: Resource) {
        _state.value = _state.value.let { s ->
            when (resource) {
                Resource.IRON     -> s.copy(iron     = s.iron     + max(1.0, s.ironPerSec)   * 1.5)
                Resource.QUARTZ   -> s.copy(quartz   = s.quartz   + s.quartzPerSec.coerceAtLeast(0.0) * 1.5)
                Resource.ENERGY   -> s.copy(energy   = s.energy   + s.energyPerSec           * 1.5)
                Resource.TITANIUM -> s.copy(titanium = s.titanium + s.titaniumPerSec         * 1.5)
                Resource.IRIDIUM  -> s.copy(iridium  = s.iridium  + s.iridiumPerSec          * 1.5)
                Resource.XENON    -> s.copy(xenon    = s.xenon    + s.xenonPerSec            * 1.5)
            }
        }
    }

    fun buyDrillHead() {
        val s = _state.value
        val next = s.drillHeadLevel + 1
        val cost = drillHeadCosts[next] ?: return
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(drillHeadLevel = next)
    }

    fun buyPowerCore() {
        val s = _state.value
        if (s.powerCoreLevel >= 1 || s.drillHeadLevel < 2) return
        if (!s.canAfford(powerCoreCost)) return
        _state.value = s.spend(powerCoreCost).copy(powerCoreLevel = 1)
    }

    fun buySolarArray() {
        val s = _state.value
        val next = s.solarArrayLevel + 1
        if (next > 2 || s.powerCoreLevel < 1) return
        val cost = solarArrayCosts[next] ?: return
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(solarArrayLevel = next)
    }

    fun buyDeepShaft() {
        val s = _state.value
        val next = s.deepShaftLevel + 1
        if (s.drillHeadLevel < if (next == 1) 3 else 4) return
        val cost = deepShaftCosts[next] ?: return
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(deepShaftLevel = next)
    }

    fun buyRefinery() {
        val s = _state.value
        if (s.hasRefinery || s.drillHeadLevel < 3) return
        if (!s.canAfford(refineryCost)) return
        _state.value = s.spend(refineryCost).copy(hasRefinery = true)
    }

    fun buyLaunchSiloA() {
        val s = _state.value
        if (s.hasLaunchSilo || s.drillHeadLevel < 3 || s.deepShaftLevel < 1) return
        if (!s.canAfford(launchSiloCostA)) return
        _state.value = s.spend(launchSiloCostA).copy(hasLaunchSilo = true)
    }

    fun buyLaunchSiloB() {
        val s = _state.value
        if (s.hasLaunchSilo || s.drillHeadLevel < 3) return
        if (!s.canAfford(launchSiloCostB)) return
        _state.value = s.spend(launchSiloCostB).copy(hasLaunchSilo = true)
    }

    fun buyRelaySatellite() {
        val s = _state.value
        if (s.hasRelaySatellite || !s.hasLaunchSilo) return
        if (!s.canAfford(relaySatelliteCost)) return
        _state.value = s.spend(relaySatelliteCost).copy(hasRelaySatellite = true)
    }

    fun buyOrbitalLab() {
        val s = _state.value
        if (s.hasOrbitalLab || !s.hasRelaySatellite) return
        if (!s.canAfford(orbitalLabCost)) return
        _state.value = s.spend(orbitalLabCost).copy(hasOrbitalLab = true)
    }

    fun buyAsteroidMiner() {
        val s = _state.value
        if (s.hasAsteroidMiner || !s.hasOrbitalLab) return
        if (!s.canAfford(asteroidMinerCost)) return
        _state.value = s.spend(asteroidMinerCost).copy(hasAsteroidMiner = true)
    }

    fun buyOrbitalSolarStation() {
        val s = _state.value
        if (s.hasOrbitalSolarStation || !s.hasOrbitalLab || s.solarArrayLevel < 2) return
        if (!s.canAfford(orbitalSolarStationCost)) return
        _state.value = s.spend(orbitalSolarStationCost).copy(hasOrbitalSolarStation = true)
    }

    fun buyCoreTap() {
        val s = _state.value
        if (s.hasCoreTap || s.drillHeadLevel < 4) return
        if (!s.canAfford(coreTapCost)) return
        _state.value = s.spend(coreTapCost).copy(hasCoreTap = true)
    }

    fun buyPlanetCore() {
        val s = _state.value
        if (s.hasPlanetCore || !s.hasCoreTap) return
        if (!s.canAfford(planetCoreCost)) return
        _state.value = s.spend(planetCoreCost).copy(hasPlanetCore = true)
    }

    fun ascend() {
        val s = _state.value
        if (!s.hasPlanetCore) return
        _state.value = GameState(stellarShards = s.stellarShards + 1, buildingSeed = System.nanoTime())
        saveState()
    }

    // Refinery: convert 1 unit of a resource downward in the chain
    fun refineryConvert(from: Resource) {
        val s = _state.value
        if (!s.hasRefinery) return
        val (cost, gain) = refineryRates[from] ?: return
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(
            iron     = s.iron     - cost.iron     + gain.iron,
            quartz   = s.quartz   - cost.quartz   + gain.quartz,
            titanium = s.titanium - cost.titanium + gain.titanium,
            iridium  = s.iridium  - cost.iridium  + gain.iridium
        )
    }

    fun hardReset() {
        _state.value = GameState()
        prefs.edit().remove("game_state").apply()
    }
}
