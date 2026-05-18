package com.lugkit.stellarextraction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow

data class GameState(
    val iron: Double = 0.0,
    val ironPerClick: Double = 1.0,
    val ironPerSecond: Double = 0.0,
    val drillLevel: Int = 0,
    val drillCost: Double = 10.0
)

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.value = _state.value.let { s ->
                    if (s.ironPerSecond > 0) s.copy(iron = s.iron + s.ironPerSecond) else s
                }
            }
        }
    }

    fun mine() {
        _state.value = _state.value.let { s ->
            s.copy(iron = s.iron + s.ironPerClick)
        }
    }

    fun upgradeDrill() {
        val s = _state.value
        if (s.iron >= s.drillCost) {
            val newLevel = s.drillLevel + 1
            _state.value = s.copy(
                iron = s.iron - s.drillCost,
                ironPerSecond = 0.1 * 1.1.pow(newLevel - 1),
                drillLevel = newLevel,
                drillCost = s.drillCost * 1.15
            )
        }
    }
}
