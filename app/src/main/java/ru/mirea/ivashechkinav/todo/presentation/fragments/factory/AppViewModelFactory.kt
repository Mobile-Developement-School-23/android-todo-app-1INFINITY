package ru.mirea.ivashechkinav.todo.presentation.fragments.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.mirea.ivashechkinav.todo.di.components.AppScope
import javax.inject.Inject
import javax.inject.Provider

@AppScope
class AppViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass] ?: throw  AssertionError()
        @Suppress("UNCHECKED_CAST")
        return creator?.get() as T
    }
}