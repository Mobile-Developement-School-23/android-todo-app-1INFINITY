package ru.mirea.ivashechkinav.todo.di.components

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.mirea.ivashechkinav.todo.App
import ru.mirea.ivashechkinav.todo.di.modules.DataModule
import ru.mirea.ivashechkinav.todo.di.modules.ViewModelModule
import ru.mirea.ivashechkinav.todo.presentation.notifications.AlarmReceiver
import javax.inject.Qualifier
import javax.inject.Scope


@Scope
annotation class AppScope

@Qualifier
annotation class AppContext

@AppScope
@Component(modules = [DataModule::class, ViewModelModule::class])
interface AppComponent {

    fun activityComponentFactory(): ActivityComponent.Factory

    fun inject(application: App)
    fun inject(alarmReceiver: AlarmReceiver)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            @AppContext
            appContext: Context
        ): AppComponent
    }
}