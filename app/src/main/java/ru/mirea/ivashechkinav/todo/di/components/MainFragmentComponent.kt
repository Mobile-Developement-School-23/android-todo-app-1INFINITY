package ru.mirea.ivashechkinav.todo.di.components

import dagger.BindsInstance
import dagger.Subcomponent
import ru.mirea.ivashechkinav.todo.di.modules.FragmentsModule
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainFragment
import javax.inject.Scope

@Scope
annotation class MainFragmentScope

@Subcomponent(modules = [FragmentsModule::class])
@MainFragmentScope
interface MainFragmentComponent {

    fun inject(mainFragment: MainFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance fragment: MainFragment): MainFragmentComponent
    }
}