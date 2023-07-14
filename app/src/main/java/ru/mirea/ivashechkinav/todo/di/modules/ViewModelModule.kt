package ru.mirea.ivashechkinav.todo.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.mirea.ivashechkinav.todo.presentation.fragments.factory.AppViewModelFactory
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainViewModel
import ru.mirea.ivashechkinav.todo.presentation.fragments.settings.SettingsViewModel
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskViewModel

@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(TaskViewModel::class)
    fun bindTaskViewModel(taskViewModel: TaskViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @Binds
    fun bindViewModelFactory(factory: AppViewModelFactory): ViewModelProvider.Factory
}