package ru.mirea.ivashechkinav.todo.di.modules

import dagger.Binds
import dagger.Module
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl
import ru.mirea.ivashechkinav.todo.di.components.AppScope
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository

@Module(includes = [LocalStorageModule::class, RemoteStorageModule::class])
interface DataModule {
    @Binds
    @AppScope
    fun bindRepository(impl: TodoItemsRepositoryImpl): TodoItemsRepository
}