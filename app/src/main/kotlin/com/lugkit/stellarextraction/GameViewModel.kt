package com.lugkit.stellarextraction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

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
    val deepShaftLevel: Int = 0,
    val hasLaunchSilo: Boolean = false,
    val hasRelaySatellite: Boolean = false,
    val hasOrbitalLab: Boolean = false,
    val hasAsteroidMiner: Boolean = false,
    val hasCoreTap: Boolean = false,
    val hasPlanetCore: Boolean = false
) {
    val ironPerSec: Double get() = when (drillHeadLevel) {
        1 -> 1.0; 2 -> 3.0; 3 -> 9.0; 4 -> 27.0; else -> 0.0
    }
    val quartzPerSec: Double get() = if (drillHeadLevel >= 2) 0.5 else 0.0
    val energyPerSec: Double get() = if (powerCoreLevel >= 1) 1.0 else 0.0
    val titaniumPerSec: Double get() = if (deepShaftLevel >= 1) 0.2 else 0.0
    val iridiumPerSec: Double get() = if (deepShaftLevel >= 2) 0.05 else 0.0
    val xenonPerSec: Double get() = if (hasAsteroidMiner) 0.02 else 0.0

    val quartzVisible: Boolean get() = drillHeadLevel >= 2
    val energyVisible: Boolean get() = powerCoreLevel >= 1
    val titaniumVisible: Boolean get() = deepShaftLevel >= 1
    val iridiumVisible: Boolean get() = deepShaftLevel >= 2
    val xenonVisible: Boolean get() = hasAsteroidMiner
    val stellarShardsVisible: Boolean get() = stellarShards > 0

    fun canAfford(cost: BuildCost): Boolean =
        iron >= cost.iron && quartz >= cost.quartz && titanium >= cost.titanium &&
        energy >= cost.energy && iridium >= cost.iridium && xenon >= cost.xenon

    fun spend(cost: BuildCost): GameState = copy(
        iron = iron - cost.iron,
        quartz = quartz - cost.quartz,
        titanium = titanium - cost.titanium,
        energy = energy - cost.energy,
        iridium = iridium - cost.iridium,
        xenon = xenon - cost.xenon
    )
}

val drillHeadCosts = mapOf(
    1 to BuildCost(iron = 10.0),
    2 to BuildCost(iron = 400.0),
    3 to BuildCost(iron = 5_000.0, energy = 80.0),
    4 to BuildCost(iron = 28_000.0, titanium = 1_000.0)
)

val deepShaftCosts = mapOf(
    1 to BuildCost(iron = 8_000.0),
    2 to BuildCost(iron = 40_000.0, titanium = 1_000.0)
)

val powerCoreCost      = BuildCost(iron = 1_500.0,  quartz = 200.0)
val launchSiloCost     = BuildCost(iron = 28_000.0, titanium = 500.0)
val relaySatelliteCost = BuildCost(iron = 80_000.0, titanium = 2_000.0)
val orbitalLabCost     = BuildCost(iridium = 2_000.0)
val asteroidMinerCost  = BuildCost(iridium = 10_000.0)
val coreTapCost        = BuildCost(xenon = 500.0)
val planetCoreCost     = BuildCost(xenon = 2_000.0)

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.value = _state.value.let { s ->
                    s.copy(
                        iron     = s.iron     + s.ironPerSec,
                        quartz   = s.quartz   + s.quartzPerSec,
                        energy   = s.energy   + s.energyPerSec,
                        titanium = s.titanium + s.titaniumPerSec,
                        iridium  = s.iridium  + s.iridiumPerSec,
                        xenon    = s.xenon    + s.xenonPerSec
                    )
                }
            }
        }
    }

    fun strike() {
        _state.value = _state.value.let { s ->
            s.copy(
                iron     = s.iron     + max(1.0, s.ironPerSec),
                quartz   = s.quartz   + s.quartzPerSec,
                energy   = s.energy   + s.energyPerSec,
                titanium = s.titanium + s.titaniumPerSec,
                iridium  = s.iridium  + s.iridiumPerSec,
                xenon    = s.xenon    + s.xenonPerSec
            )
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

    fun buyDeepShaft() {
        val s = _state.value
        val next = s.deepShaftLevel + 1
        val minDrill = if (next == 1) 3 else 4
        if (s.drillHeadLevel < minDrill) return
        val cost = deepShaftCosts[next] ?: return
        if (!s.canAfford(cost)) return
        _state.value = s.spend(cost).copy(deepShaftLevel = next)
    }

    fun buyLaunchSilo() {
        val s = _state.value
        if (s.hasLaunchSilo || s.deepShaftLevel < 1) return
        if (!s.canAfford(launchSiloCost)) return
        _state.value = s.spend(launchSiloCost).copy(hasLaunchSilo = true)
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
        _state.value = GameState(stellarShards = s.stellarShards + 1)
    }
}
