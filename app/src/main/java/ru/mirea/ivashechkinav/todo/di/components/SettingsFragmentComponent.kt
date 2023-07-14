package ru.mirea.ivashechkinav.todo.di.components

import dagger.Subcomponent
import ru.mirea.ivashechkinav.todo.presentation.fragments.settings.SettingsFragment
import javax.inject.Scope

@Scope
annotation class SettingsFragmentScope

@Subcomponent
@SettingsFragmentScope
interface SettingsFragmentComponent {

    fun inject(settingsFragment: SettingsFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): SettingsFragmentComponent
    }
}