package ru.mirea.ivashechkinav.todo.di.components

import dagger.Subcomponent
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainFragment
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskFragment
import javax.inject.Scope

@Scope
annotation class TaskFragmentScope

@Subcomponent
@TaskFragmentScope
interface TaskFragmentComponent {

    fun inject(taskFragment: TaskFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(): TaskFragmentComponent
    }
}