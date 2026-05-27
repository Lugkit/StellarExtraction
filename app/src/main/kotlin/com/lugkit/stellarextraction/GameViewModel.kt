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

enum class AscUpgrade {
    // Mining
    STRIKE_YIELD, DRILL_SPEED, DEEP_SHAFT_SPEED, QUARTZ_RICHNESS,
    IRIDIUM_CONCENTRATION, TITANIUM_DENSITY,
    // Construction
    LAUNCH_EFFICIENCY, POWER_CORE_COST, REFINERY_COST,
    // Orbital
    SATELLITE_UPLINK, ORBITAL_LAB_RESEARCH, SOLAR_AMPLIFIER,
    // Meta
    HEAD_START, RESONANCE
}

const val ASC_MAX_LEVEL = 4

// bonus = 1 - 1/(1 + level * 0.28)  → lv1 ≈ 22%, lv2 ≈ 36%, lv3 ≈ 46%, lv4 ≈ 53%
fun ascBonus(level: Int): Double =
    if (level == 0) 0.0 else 1.0 - 1.0 / (1.0 + level * 0.28)

fun ascBonusPct(level: Int): String =
    if (level == 0) "—" else "+${(ascBonus(level) * 100).toInt()}%"

data class BuildCost(
    val iron: Double = 0.0,
    val quartz: Double = 0.0,
    val titanium: Double = 0.0,
    val energy: Double = 0.0,
    val iridium: Double = 0.0,
    val xenon: Double = 0.0
) {
    fun scale(factor: Double) = BuildCost(
        iron     = (iron     * factor).coerceAtLeast(0.0),
        quartz   = (quartz   * factor).coerceAtLeast(0.0),
        titanium = (titanium * factor).coerceAtLeast(0.0),
        energy   = (energy   * factor).coerceAtLeast(0.0),
        iridium  = (iridium  * factor).coerceAtLeast(0.0),
        xenon    = (xenon    * factor).coerceAtLeast(0.0)
    )
}

data class GameState(
    // ── Resources ────────────────────────────────────────────────────────────
    val iron: Double = 0.0,
    val quartz: Double = 0.0,
    val titanium: Double = 0.0,
    val iridium: Double = 0.0,
    val xenon: Double = 0.0,
    val energy: Double = 0.0,
    val stellarShards: Int = 0,

    // ── Buildings ─────────────────────────────────────────────────────────────
    val drillHeadLevel: Int = 0,
    val powerCoreLevel: Int = 0,
    val solarArrayLevel: Int = 0,
    val deepShaftLevel: Int = 0,
    val hasLaunchSilo: Boolean = false,
    val hasRelaySatellite: Boolean = false,
    val hasOrbitalLab: Boolean = false,
    val hasAsteroidMiner: Boolean = false,
    val hasOrbitalSolarStation: Boolean = false,
    val hasRefinery: Boolean = false,
    val hasCoreTap: Boolean = false,
    val hasPlanetCore: Boolean = false,
    val hasOrbitalBeacon: Boolean = false,
    val beaconCompleteAt: Long = 0L,
    val buildingSeed: Long = 12345L,

    // ── Continuous mine levels (reset on ascension) ───────────────────────────
    val quartzVeinLevel: Int = 0,
    val titaniumShaftLevel: Int = 0,
    val iridiumDepositLevel: Int = 0,
    val xenonExtractorLevel: Int = 0,

    // ── Ascension upgrades (persist across runs) ──────────────────────────────
    val strikeYieldLevel: Int = 0,
    val drillSpeedLevel: Int = 0,
    val deepShaftSpeedLevel: Int = 0,
    val quartzRichnessLevel: Int = 0,
    val iridiumConcentrationLevel: Int = 0,
    val titaniumDensityLevel: Int = 0,
    val launchEfficiencyLevel: Int = 0,
    val powerCoreCostLevel: Int = 0,
    val refineryCostLevel: Int = 0,
    val satelliteUplinkLevel: Int = 0,
    val orbitalLabResearchLevel: Int = 0,
    val solarAmplifierLevel: Int = 0,
    val headStartLevel: Int = 0,
    val resonanceLevel: Int = 0
) {
    // ── Production rates (with ascension bonuses) ─────────────────────────────
    val ironPerSec: Double get() =
        drillHeadIronRate(drillHeadLevel) * (1 + ascBonus(drillSpeedLevel))

    val quartzBasePerSec: Double get() {
        val fromDrill = if (drillHeadLevel >= 2) 1.5 * (1 + ascBonus(quartzRichnessLevel)) else 0.0
        val fromVein  = quartzVeinQuartzRate(quartzVeinLevel) * (1 + ascBonus(quartzRichnessLevel))
        return fromDrill + fromVein
    }
    val quartzUpkeep: Double     get() = if (powerCoreLevel >= 1) 1.0 else 0.0
    val quartzPerSec: Double     get() = quartzBasePerSec - quartzUpkeep

    val solarPerSec: Double get() =
        solarArrayLevel.toDouble() * (1 + ascBonus(solarAmplifierLevel))
    val energyPerSec: Double get() =
        (if (powerCoreLevel >= 1) 3.0 else 0.0) + solarPerSec +
        (if (hasOrbitalSolarStation) 20.0 else 0.0)

    val titaniumPerSec: Double get() {
        val fromShaft = if (deepShaftLevel >= 1) 0.5 * (1 + ascBonus(deepShaftSpeedLevel)) * (1 + ascBonus(titaniumDensityLevel)) else 0.0
        val fromMine  = titaniumShaftTitaniumRate(titaniumShaftLevel) * (1 + ascBonus(titaniumDensityLevel))
        return fromShaft + fromMine
    }
    val iridiumPerSec: Double get() {
        val fromShaft   = if (deepShaftLevel >= 2) 0.15 * (1 + ascBonus(deepShaftSpeedLevel)) * (1 + ascBonus(iridiumConcentrationLevel)) * (1 + ascBonus(orbitalLabResearchLevel)) else 0.0
        val fromDeposit = iridiumDepositIridiumRate(iridiumDepositLevel) * (1 + ascBonus(iridiumConcentrationLevel))
        return fromShaft + fromDeposit
    }
    val xenonPerSec: Double get() {
        val fromMiner     = if (hasAsteroidMiner) 0.05 * (1 + ascBonus(satelliteUplinkLevel)) else 0.0
        val fromExtractor = xenonExtractorXenonRate(xenonExtractorLevel) * (1 + ascBonus(satelliteUplinkLevel))
        return fromMiner + fromExtractor
    }

    // ── Visibility ────────────────────────────────────────────────────────────
    val quartzVisible: Boolean        get() = drillHeadLevel >= 2
    val energyVisible: Boolean        get() = powerCoreLevel >= 1 || solarArrayLevel >= 1
    val titaniumVisible: Boolean      get() = deepShaftLevel >= 1
    val iridiumVisible: Boolean       get() = deepShaftLevel >= 2
    val xenonVisible: Boolean         get() = hasAsteroidMiner
    val stellarShardsVisible: Boolean get() = stellarShards > 0

    // ── Ascension upgrade level lookup ────────────────────────────────────────
    fun ascLevel(u: AscUpgrade): Int = when (u) {
        AscUpgrade.STRIKE_YIELD          -> strikeYieldLevel
        AscUpgrade.DRILL_SPEED           -> drillSpeedLevel
        AscUpgrade.DEEP_SHAFT_SPEED      -> deepShaftSpeedLevel
        AscUpgrade.QUARTZ_RICHNESS       -> quartzRichnessLevel
        AscUpgrade.IRIDIUM_CONCENTRATION -> iridiumConcentrationLevel
        AscUpgrade.TITANIUM_DENSITY      -> titaniumDensityLevel
        AscUpgrade.LAUNCH_EFFICIENCY     -> launchEfficiencyLevel
        AscUpgrade.POWER_CORE_COST       -> powerCoreCostLevel
        AscUpgrade.REFINERY_COST         -> refineryCostLevel
        AscUpgrade.SATELLITE_UPLINK      -> satelliteUplinkLevel
        AscUpgrade.ORBITAL_LAB_RESEARCH  -> orbitalLabResearchLevel
        AscUpgrade.SOLAR_AMPLIFIER       -> solarAmplifierLevel
        AscUpgrade.HEAD_START            -> headStartLevel
        AscUpgrade.RESONANCE             -> resonanceLevel
    }

    // ── Effective costs (with construction discounts) ─────────────────────────
    fun effectivePowerCoreCost()  = powerCoreCost.scale(1 - ascBonus(powerCoreCostLevel))
    fun effectiveLaunchSiloCostA() = launchSiloCostA.scale(1 - ascBonus(launchEfficiencyLevel))
    fun effectiveLaunchSiloCostB() = launchSiloCostB.scale(1 - ascBonus(launchEfficiencyLevel))
    fun effectiveRefineryCost()   = refineryCost.scale(1 - ascBonus(refineryCostLevel))

    // ── Core helpers ──────────────────────────────────────────────────────────
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

    // Copy all ascension upgrades (used by ascend())
    fun ascFields() = listOf(
        strikeYieldLevel, drillSpeedLevel, deepShaftSpeedLevel, quartzRichnessLevel,
        iridiumConcentrationLevel, titaniumDensityLevel, launchEfficiencyLevel,
        powerCoreCostLevel, refineryCostLevel, satelliteUplinkLevel,
        orbitalLabResearchLevel, solarAmplifierLevel, headStartLevel, resonanceLevel
    )
}

// ── Costs ─────────────────────────────────────────────────────────────────────

// ── Orbital Beacon config (placeholder — adjust after playtesting) ────────────
const val BEACON_BASE_TIME      = 7200L   // seconds (2 hours)
const val BEACON_TIME_INCREMENT = 3600L   // extra seconds per stellar shard (1 hour)
val orbitalBeaconCost = BuildCost(
    iron     = 500_000.0,
    quartz   =  50_000.0,
    titanium =  10_000.0,
    iridium  =  25_000.0,
    xenon    =   5_000.0
)
fun beaconBuildSeconds(stellarShards: Int): Long =
    BEACON_BASE_TIME + stellarShards.toLong() * BEACON_TIME_INCREMENT
// ─────────────────────────────────────────────────────────────────────────────

// ── Continuous mine helpers ───────────────────────────────────────────────────

private const val MINE_SCALE = 1.15

fun drillHeadNextCost(level: Int)         = BuildCost(iron = 10.0 * Math.pow(MINE_SCALE, level.toDouble()))
fun quartzVeinNextCost(level: Int)        = BuildCost(iron = 200.0 * Math.pow(MINE_SCALE, level.toDouble()), quartz = 50.0 * Math.pow(MINE_SCALE, level.toDouble()))
fun titaniumShaftNextCost(level: Int)     = BuildCost(iron = 1_000.0 * Math.pow(MINE_SCALE, level.toDouble()), quartz = 200.0 * Math.pow(MINE_SCALE, level.toDouble()))
fun iridiumDepositNextCost(level: Int)    = BuildCost(iron = 5_000.0 * Math.pow(MINE_SCALE, level.toDouble()), titanium = 500.0 * Math.pow(MINE_SCALE, level.toDouble()))
fun xenonExtractorNextCost(level: Int)    = BuildCost(iridium = 2_000.0 * Math.pow(MINE_SCALE, level.toDouble()), xenon = 200.0 * Math.pow(MINE_SCALE, level.toDouble()))

fun drillHeadIronRate(level: Int)         = if (level == 0) 0.0 else 1.0 * Math.pow(MINE_SCALE, level.toDouble())
fun quartzVeinQuartzRate(level: Int)      = if (level == 0) 0.0 else 0.5 * Math.pow(MINE_SCALE, level.toDouble())
fun titaniumShaftTitaniumRate(level: Int) = if (level == 0) 0.0 else 0.3 * Math.pow(MINE_SCALE, level.toDouble())
fun iridiumDepositIridiumRate(level: Int) = if (level == 0) 0.0 else 0.2 * Math.pow(MINE_SCALE, level.toDouble())
fun xenonExtractorXenonRate(level: Int)   = if (level == 0) 0.0 else 0.1 * Math.pow(MINE_SCALE, level.toDouble())

// ─────────────────────────────────────────────────────────────────────────────

val deepShaftCosts = mapOf(
    1 to BuildCost(iron =  8_000.0),
    2 to BuildCost(iron = 40_000.0, titanium = 1_000.0)
)
val powerCoreCost           = BuildCost(iron = 1_500.0, quartz = 200.0)
val solarArrayCosts         = mapOf(1 to BuildCost(quartz = 800.0), 2 to BuildCost(quartz = 2_000.0))
val launchSiloCostA         = BuildCost(iron = 28_000.0, titanium = 500.0)
val launchSiloCostB         = BuildCost(iron = 15_000.0, quartz = 2_000.0)
val relaySatelliteCost      = BuildCost(iron = 80_000.0, titanium = 2_000.0)
val orbitalLabCost          = BuildCost(iridium = 2_000.0)
val asteroidMinerCost       = BuildCost(iridium = 10_000.0)
val orbitalSolarStationCost = BuildCost(iridium = 3_000.0)
val refineryCost            = BuildCost(iron = 3_000.0)
val coreTapCost             = BuildCost(xenon = 500.0)
val planetCoreCost          = BuildCost(xenon = 2_000.0)

val refineryRates = mapOf(
    Resource.IRIDIUM  to Pair(BuildCost(iridium  = 1.0), BuildCost(titanium = 3.0)),
    Resource.TITANIUM to Pair(BuildCost(titanium = 1.0), BuildCost(quartz   = 5.0)),
    Resource.QUARTZ   to Pair(BuildCost(quartz   = 1.0), BuildCost(iron     = 5.0))
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

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
                _state.value = _state.value.applyProduction(1.0).let { s ->
                    if (!s.hasOrbitalBeacon && s.beaconCompleteAt > 0L && System.currentTimeMillis() >= s.beaconCompleteAt)
                        s.copy(hasOrbitalBeacon = true, beaconCompleteAt = 0L)
                    else s
                }
                if (++tick % 30 == 0) saveState()
            }
        }
    }

    fun saveState() {
        try {
            val s = _state.value
            val json = JSONObject().apply {
                put("iron", s.iron); put("quartz", s.quartz); put("titanium", s.titanium)
                put("iridium", s.iridium); put("xenon", s.xenon); put("energy", s.energy)
                put("stellarShards", s.stellarShards)
                put("drillHeadLevel", s.drillHeadLevel)
                put("powerCoreLevel", s.powerCoreLevel)
                put("solarArrayLevel", s.solarArrayLevel)
                put("deepShaftLevel", s.deepShaftLevel)
                put("hasLaunchSilo", s.hasLaunchSilo)
                put("hasRelaySatellite", s.hasRelaySatellite)
                put("hasOrbitalLab", s.hasOrbitalLab)
                put("hasAsteroidMiner", s.hasAsteroidMiner)
                put("hasOrbitalSolarStation", s.hasOrbitalSolarStation)
                put("hasRefinery", s.hasRefinery)
                put("hasCoreTap", s.hasCoreTap)
                put("hasPlanetCore", s.hasPlanetCore)
                put("hasOrbitalBeacon", s.hasOrbitalBeacon)
                put("beaconCompleteAt", s.beaconCompleteAt)
                put("buildingSeed", s.buildingSeed)
                put("quartzVeinLevel", s.quartzVeinLevel)
                put("titaniumShaftLevel", s.titaniumShaftLevel)
                put("iridiumDepositLevel", s.iridiumDepositLevel)
                put("xenonExtractorLevel", s.xenonExtractorLevel)
                // Ascension upgrades
                put("asc_strikeYield", s.strikeYieldLevel)
                put("asc_drillSpeed", s.drillSpeedLevel)
                put("asc_deepShaftSpeed", s.deepShaftSpeedLevel)
                put("asc_quartzRichness", s.quartzRichnessLevel)
                put("asc_iridiumConc", s.iridiumConcentrationLevel)
                put("asc_titaniumDensity", s.titaniumDensityLevel)
                put("asc_launchEfficiency", s.launchEfficiencyLevel)
                put("asc_powerCoreCost", s.powerCoreCostLevel)
                put("asc_refineryCost", s.refineryCostLevel)
                put("asc_satelliteUplink", s.satelliteUplinkLevel)
                put("asc_orbitalLabResearch", s.orbitalLabResearchLevel)
                put("asc_solarAmplifier", s.solarAmplifierLevel)
                put("asc_headStart", s.headStartLevel)
                put("asc_resonance", s.resonanceLevel)
                put("savedAt", System.currentTimeMillis())
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
                hasOrbitalBeacon       = obj.optBoolean("hasOrbitalBeacon", false),
                beaconCompleteAt       = obj.optLong("beaconCompleteAt", 0L),
                buildingSeed            = obj.optLong("buildingSeed", 12345L),
                quartzVeinLevel         = obj.optInt("quartzVeinLevel", 0),
                titaniumShaftLevel      = obj.optInt("titaniumShaftLevel", 0),
                iridiumDepositLevel     = obj.optInt("iridiumDepositLevel", 0),
                xenonExtractorLevel     = obj.optInt("xenonExtractorLevel", 0),
                // Ascension upgrades
                strikeYieldLevel          = obj.optInt("asc_strikeYield", 0),
                drillSpeedLevel           = obj.optInt("asc_drillSpeed", 0),
                deepShaftSpeedLevel       = obj.optInt("asc_deepShaftSpeed", 0),
                quartzRichnessLevel       = obj.optInt("asc_quartzRichness", 0),
                iridiumConcentrationLevel = obj.optInt("asc_iridiumConc", 0),
                titaniumDensityLevel      = obj.optInt("asc_titaniumDensity", 0),
                launchEfficiencyLevel     = obj.optInt("asc_launchEfficiency", 0),
                powerCoreCostLevel        = obj.optInt("asc_powerCoreCost", 0),
                refineryCostLevel         = obj.optInt("asc_refineryCost", 0),
                satelliteUplinkLevel      = obj.optInt("asc_satelliteUplink", 0),
                orbitalLabResearchLevel   = obj.optInt("asc_orbitalLabResearch", 0),
                solarAmplifierLevel       = obj.optInt("asc_solarAmplifier", 0),
                headStartLevel            = obj.optInt("asc_headStart", 0),
                resonanceLevel            = obj.optInt("asc_resonance", 0)
            )
            val withProd = if (elapsed > 1) s.applyProduction(elapsed) else s
            _state.value = if (!withProd.hasOrbitalBeacon && withProd.beaconCompleteAt > 0L && System.currentTimeMillis() >= withProd.beaconCompleteAt)
                withProd.copy(hasOrbitalBeacon = true, beaconCompleteAt = 0L)
            else withProd
        } catch (_: Exception) {}
    }

    // ── Mining ────────────────────────────────────────────────────────────────

    fun strike() {
        _state.value = _state.value.let { s ->
            val mult = 0.5 * (1 + ascBonus(s.strikeYieldLevel))
            s.copy(
                iron     = s.iron     + max(1.0, s.ironPerSec)               * mult,
                quartz   = (s.quartz  + s.quartzPerSec                       * mult).coerceAtLeast(0.0),
                energy   = s.energy   + s.energyPerSec                       * mult,
                titanium = s.titanium + s.titaniumPerSec                     * mult,
                iridium  = s.iridium  + s.iridiumPerSec                      * mult,
                xenon    = s.xenon    + s.xenonPerSec                        * mult
            )
        }
    }

    fun focusedStrike(resource: Resource) {
        _state.value = _state.value.let { s ->
            val mult = 1.5 * (1 + ascBonus(s.resonanceLevel))
            when (resource) {
                Resource.IRON     -> s.copy(iron     = s.iron     + max(1.0, s.ironPerSec)               * mult)
                Resource.QUARTZ   -> s.copy(quartz   = (s.quartz  + s.quartzPerSec.coerceAtLeast(0.0)   * mult).coerceAtLeast(0.0))
                Resource.ENERGY   -> s.copy(energy   = s.energy   + s.energyPerSec                       * mult)
                Resource.TITANIUM -> s.copy(titanium = s.titanium + s.titaniumPerSec                     * mult)
                Resource.IRIDIUM  -> s.copy(iridium  = s.iridium  + s.iridiumPerSec                      * mult)
                Resource.XENON    -> s.copy(xenon    = s.xenon    + s.xenonPerSec                        * mult)
            }
        }
    }

    // ── Shop purchases ────────────────────────────────────────────────────────

    fun buyDrillHead() {
        val s = _state.value
        val cost = drillHeadNextCost(s.drillHeadLevel)
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(drillHeadLevel = s.drillHeadLevel + 1)
    }

    fun buyQuartzVein() {
        val s = _state.value
        if (s.drillHeadLevel < 2) return
        val cost = quartzVeinNextCost(s.quartzVeinLevel)
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(quartzVeinLevel = s.quartzVeinLevel + 1)
    }

    fun buyTitaniumShaft() {
        val s = _state.value
        if (s.deepShaftLevel < 1) return
        val cost = titaniumShaftNextCost(s.titaniumShaftLevel)
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(titaniumShaftLevel = s.titaniumShaftLevel + 1)
    }

    fun buyIridiumDeposit() {
        val s = _state.value
        if (s.deepShaftLevel < 2) return
        val cost = iridiumDepositNextCost(s.iridiumDepositLevel)
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(iridiumDepositLevel = s.iridiumDepositLevel + 1)
    }

    fun buyXenonExtractor() {
        val s = _state.value
        if (!s.hasAsteroidMiner) return
        val cost = xenonExtractorNextCost(s.xenonExtractorLevel)
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(xenonExtractorLevel = s.xenonExtractorLevel + 1)
    }

    fun buyPowerCore() {
        val s = _state.value
        if (s.powerCoreLevel >= 1 || s.drillHeadLevel < 2) return
        val cost = s.effectivePowerCoreCost()
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(powerCoreLevel = 1)
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
        val cost = s.effectiveRefineryCost()
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(hasRefinery = true)
    }

    fun buyLaunchSiloA() {
        val s = _state.value
        if (s.hasLaunchSilo || s.drillHeadLevel < 3 || s.deepShaftLevel < 1) return
        val cost = s.effectiveLaunchSiloCostA()
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(hasLaunchSilo = true)
    }

    fun buyLaunchSiloB() {
        val s = _state.value
        if (s.hasLaunchSilo || s.drillHeadLevel < 3) return
        val cost = s.effectiveLaunchSiloCostB()
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(hasLaunchSilo = true)
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

    fun buyOrbitalBeacon() {
        val s = _state.value
        if (s.hasOrbitalBeacon || s.beaconCompleteAt > 0L) return
        if (!s.hasPlanetCore || !s.hasOrbitalLab) return
        if (!s.canAfford(orbitalBeaconCost)) return
        val completeAt = System.currentTimeMillis() + beaconBuildSeconds(s.stellarShards) * 1000L
        _state.value = s.spend(orbitalBeaconCost).copy(beaconCompleteAt = completeAt)
    }

    fun ascend() {
        val s = _state.value
        if (!s.hasPlanetCore || !s.hasOrbitalBeacon) return
        val headStart = when (s.headStartLevel) {
            1 -> 500.0; 2 -> 2_000.0; 3 -> 8_000.0; 4 -> 32_000.0; else -> 0.0
        }
        _state.value = GameState(
            stellarShards             = s.stellarShards + 1,
            buildingSeed              = System.nanoTime(),
            iron                      = headStart,
            // Preserve ascension upgrades
            strikeYieldLevel          = s.strikeYieldLevel,
            drillSpeedLevel           = s.drillSpeedLevel,
            deepShaftSpeedLevel       = s.deepShaftSpeedLevel,
            quartzRichnessLevel       = s.quartzRichnessLevel,
            iridiumConcentrationLevel = s.iridiumConcentrationLevel,
            titaniumDensityLevel      = s.titaniumDensityLevel,
            launchEfficiencyLevel     = s.launchEfficiencyLevel,
            powerCoreCostLevel        = s.powerCoreCostLevel,
            refineryCostLevel         = s.refineryCostLevel,
            satelliteUplinkLevel      = s.satelliteUplinkLevel,
            orbitalLabResearchLevel   = s.orbitalLabResearchLevel,
            solarAmplifierLevel       = s.solarAmplifierLevel,
            headStartLevel            = s.headStartLevel,
            resonanceLevel            = s.resonanceLevel
        )
        saveState()
    }

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

    // ── Ascension Shop ────────────────────────────────────────────────────────

    fun buyAscUpgrade(upgrade: AscUpgrade) {
        val s = _state.value
        val cur = s.ascLevel(upgrade)
        if (cur >= ASC_MAX_LEVEL) return
        val cost = cur + 1  // lv1 = 1 shard, lv2 = 2, lv3 = 3, lv4 = 4
        if (s.stellarShards < cost) return
        _state.value = s.copy(
            stellarShards             = s.stellarShards - cost,
            strikeYieldLevel          = if (upgrade == AscUpgrade.STRIKE_YIELD)          cur + 1 else s.strikeYieldLevel,
            drillSpeedLevel           = if (upgrade == AscUpgrade.DRILL_SPEED)           cur + 1 else s.drillSpeedLevel,
            deepShaftSpeedLevel       = if (upgrade == AscUpgrade.DEEP_SHAFT_SPEED)      cur + 1 else s.deepShaftSpeedLevel,
            quartzRichnessLevel       = if (upgrade == AscUpgrade.QUARTZ_RICHNESS)       cur + 1 else s.quartzRichnessLevel,
            iridiumConcentrationLevel = if (upgrade == AscUpgrade.IRIDIUM_CONCENTRATION) cur + 1 else s.iridiumConcentrationLevel,
            titaniumDensityLevel      = if (upgrade == AscUpgrade.TITANIUM_DENSITY)      cur + 1 else s.titaniumDensityLevel,
            launchEfficiencyLevel     = if (upgrade == AscUpgrade.LAUNCH_EFFICIENCY)     cur + 1 else s.launchEfficiencyLevel,
            powerCoreCostLevel        = if (upgrade == AscUpgrade.POWER_CORE_COST)       cur + 1 else s.powerCoreCostLevel,
            refineryCostLevel         = if (upgrade == AscUpgrade.REFINERY_COST)         cur + 1 else s.refineryCostLevel,
            satelliteUplinkLevel      = if (upgrade == AscUpgrade.SATELLITE_UPLINK)      cur + 1 else s.satelliteUplinkLevel,
            orbitalLabResearchLevel   = if (upgrade == AscUpgrade.ORBITAL_LAB_RESEARCH)  cur + 1 else s.orbitalLabResearchLevel,
            solarAmplifierLevel       = if (upgrade == AscUpgrade.SOLAR_AMPLIFIER)       cur + 1 else s.solarAmplifierLevel,
            headStartLevel            = if (upgrade == AscUpgrade.HEAD_START)            cur + 1 else s.headStartLevel,
            resonanceLevel            = if (upgrade == AscUpgrade.RESONANCE)             cur + 1 else s.resonanceLevel
        )
        saveState()
    }

    fun hardReset() {
        _state.value = GameState()
        prefs.edit().remove("game_state").apply()
    }
}
