package dev.parez.navbarz.sample

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
 * The knobs on the Settings screen. In-memory only — the demo has no persistence layer, and a
 * per-platform one would be more setup than a navigation sample needs.
 */
class SettingsState {
    private val _unit = MutableStateFlow(UnitSystem.METRIC)
    val unit: StateFlow<UnitSystem> = _unit.asStateFlow()

    private val _twentyFourHourTime = MutableStateFlow(false)
    val twentyFourHourTime: StateFlow<Boolean> = _twentyFourHourTime.asStateFlow()

    private val _mode = MutableStateFlow(ThemeMode.SYSTEM)
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    // On by default: frosting the screens is the demo's whole way of pointing at the chrome. The
    // switch exists because that also makes the content deliberately unreadable, and anyone who
    // came to read a Pokédex rather than look at a navigation bar wants it off.
    private val _blurContent = MutableStateFlow(true)
    val blurContent: StateFlow<Boolean> = _blurContent.asStateFlow()

    fun setUnit(value: UnitSystem) {
        _unit.value = value
    }

    fun setTwentyFourHourTime(value: Boolean) {
        _twentyFourHourTime.value = value
    }

    fun setMode(value: ThemeMode) {
        _mode.value = value
    }

    fun setBlurContent(value: Boolean) {
        _blurContent.value = value
    }
}
