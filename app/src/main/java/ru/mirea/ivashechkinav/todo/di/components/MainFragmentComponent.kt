package ru.mirea.ivashechkinav.todo.di.components

import dagger.Subcomponent
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainFragment
import javax.inject.Scope

@Scope
annotation class MainFragmentScope

@Subcomponent
@MainFragmentScope
interface MainFragmentComponent {

    fun inject(mainFragment: MainFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainFragmentComponent
    }
}