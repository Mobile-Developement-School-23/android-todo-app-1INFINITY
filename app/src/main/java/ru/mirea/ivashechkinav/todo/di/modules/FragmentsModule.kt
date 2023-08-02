package ru.mirea.ivashechkinav.todo.di.modules

import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import ru.mirea.ivashechkinav.todo.di.components.MainFragmentScope
import ru.mirea.ivashechkinav.todo.di.components.SettingsFragmentScope
import ru.mirea.ivashechkinav.todo.di.components.TaskFragmentScope
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainFragment
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainViewModel
import ru.mirea.ivashechkinav.todo.presentation.fragments.settings.SettingsFragment
import ru.mirea.ivashechkinav.todo.presentation.fragments.settings.SettingsViewModel
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskFragment
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskViewModel


@Module
class FragmentsModule {

    @Provides
    @MainFragmentScope
    fun provideMainViewModel(
        factory: ViewModelProvider.Factory,
        fragment: MainFragment
    ): MainViewModel = ViewModelProvider(fragment, factory)[MainViewModel::class.java]

    @Provides
    @TaskFragmentScope
    fun provideTaskViewModel(
        factory: ViewModelProvider.Factory,
        fragment: TaskFragment
    ): TaskViewModel = ViewModelProvider(fragment, factory)[TaskViewModel::class.java]

    @Provides
    @SettingsFragmentScope
    fun provideSettingsViewModel(
        factory: ViewModelProvider.Factory,
        fragment: SettingsFragment
    ): SettingsViewModel = ViewModelProvider(fragment, factory)[SettingsViewModel::class.java]
}