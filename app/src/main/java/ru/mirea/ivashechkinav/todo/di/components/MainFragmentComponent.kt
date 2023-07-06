package ru.mirea.ivashechkinav.todo.di.components

import dagger.Subcomponent
import javax.inject.Scope

@Scope
annotation class MainFragmentScope

@Subcomponent
@MainFragmentScope
interface MainFragmentComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainFragmentComponent
    }
}