package ru.mirea.ivashechkinav.todo.di.components

import dagger.Subcomponent
import javax.inject.Scope

@Scope
annotation class TaskFragmentScope

@Subcomponent
@TaskFragmentScope
interface TaskFragmentComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): TaskFragmentComponent
    }
}