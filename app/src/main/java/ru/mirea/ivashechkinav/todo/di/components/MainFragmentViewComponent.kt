package ru.mirea.ivashechkinav.todo.di.components

import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import dagger.BindsInstance
import dagger.Subcomponent
import ru.mirea.ivashechkinav.todo.databinding.FragmentMainBinding
import javax.inject.Scope

@Scope
annotation class MainFragmentViewScope

@Subcomponent
@MainFragmentViewScope
interface MainFragmentViewComponent {

    fun boot(): MainFragmentBootstrapper

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance
            navController: NavController,
            @BindsInstance
            binding: FragmentMainBinding,
            @BindsInstance
            lifecycleOwner: LifecycleOwner,
        ): MainFragmentViewComponent
    }
}