package ru.mirea.ivashechkinav.todo.di.components

import dagger.BindsInstance
import dagger.Subcomponent
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainFragment
import javax.inject.Scope

@Scope
annotation class MainFragmentScope

@Subcomponent
@MainFragmentScope
interface MainFragmentComponent {
    fun mainFragmentViewComponentFactory(): MainFragmentViewComponent.Factory

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance fragment: MainFragment): MainFragmentComponent
    }
}