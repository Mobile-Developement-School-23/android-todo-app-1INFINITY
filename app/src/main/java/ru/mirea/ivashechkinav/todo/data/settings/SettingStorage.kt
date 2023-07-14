package ru.mirea.ivashechkinav.todo.data.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.mirea.ivashechkinav.todo.di.components.AppScope
import javax.inject.Inject


enum class UiTheme(val value: Int) {
    DARK(MODE_NIGHT_YES),
    LIGHT(MODE_NIGHT_NO),
    SYSTEM(MODE_NIGHT_FOLLOW_SYSTEM);

    companion object {
        fun parseTheme(value: Int) =
            when (value) {
                MODE_NIGHT_YES -> DARK
                MODE_NIGHT_NO -> LIGHT
                MODE_NIGHT_FOLLOW_SYSTEM -> SYSTEM
                else -> throw IllegalArgumentException("Unable to parse theme")
            }
    }
}

@AppScope
class SettingStorage @Inject constructor(
    private val prefs: SharedPreferences
) {
    var theme: UiTheme = UiTheme.SYSTEM
        get() = UiTheme.parseTheme(prefs.getInt(themeKey, MODE_NIGHT_FOLLOW_SYSTEM))
        set(value) {
            prefs.edit().putInt(themeKey, value.value).apply()
            _themeFlow.update { value }
            field = value
        }
    private val _themeFlow: MutableStateFlow<UiTheme> = MutableStateFlow(theme)
    val themeFlow: StateFlow<UiTheme?> = _themeFlow.asStateFlow()

    companion object {
        const val themeKey = "UiThemeKey"
    }
}