package dev.parez.barz.sample

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class UnitSystem {
    METRIC,
    IMPERIAL,
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/**
 * The three knobs on the Settings screen. In-memory only — the demo has no persistence layer, and a
 * per-platform one would be more setup than a navigation sample needs.
 */
class SettingsState {
    private val _unit = MutableStateFlow(UnitSystem.METRIC)
    val unit: StateFlow<UnitSystem> = _unit.asStateFlow()

    private val _twentyFourHourTime = MutableStateFlow(false)
    val twentyFourHourTime: StateFlow<Boolean> = _twentyFourHourTime.asStateFlow()

    private val _mode = MutableStateFlow(ThemeMode.SYSTEM)
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setUnit(value: UnitSystem) {
        _unit.value = value
    }

    fun setTwentyFourHourTime(value: Boolean) {
        _twentyFourHourTime.value = value
    }

    fun setMode(value: ThemeMode) {
        _mode.value = value
    }
}
