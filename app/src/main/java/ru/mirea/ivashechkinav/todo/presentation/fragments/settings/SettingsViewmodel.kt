package ru.mirea.ivashechkinav.todo.presentation.fragments.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.data.settings.SettingStorage
import ru.mirea.ivashechkinav.todo.data.settings.UiTheme
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val settingStorage: SettingStorage): ViewModel() {

    sealed class UiEffect {
        object ToBackFragment: UiEffect()
    }
    val themeState = settingStorage.themeFlow

    private val _effect: Channel<UiEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    private fun setEffect(builder: () -> UiEffect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    fun navigateBack() = setEffect {
        UiEffect.ToBackFragment
    }

    fun onThemeSelected(uiTheme: UiTheme) {
        settingStorage.theme = uiTheme
    }
}