package ru.mirea.ivashechkinav.todo.di.components

import dagger.BindsInstance
import dagger.Subcomponent
import ru.mirea.ivashechkinav.todo.presentation.MainActivity
import javax.inject.Scope

@Scope
annotation class ActivityScope

@Subcomponent
@ActivityScope
interface ActivityComponent {
    fun mainFragmentComponentFactory(): MainFragmentComponent.Factory
    fun taskFragmentComponentFactory(): TaskFragmentComponent.Factory
    fun settingsFragmentComponentFactory(): SettingsFragmentComponent.Factory

    fun inject(mainActivity: MainActivity)

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance
            activity: MainActivity
        ): ActivityComponent
    }
}