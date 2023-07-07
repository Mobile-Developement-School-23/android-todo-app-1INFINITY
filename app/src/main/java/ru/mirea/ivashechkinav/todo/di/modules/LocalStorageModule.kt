package ru.mirea.ivashechkinav.todo.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.mirea.ivashechkinav.todo.data.room.AppDatabase
import ru.mirea.ivashechkinav.todo.data.room.TodoDao
import ru.mirea.ivashechkinav.todo.data.sharedprefs.RevisionRepository
import ru.mirea.ivashechkinav.todo.data.sharedprefs.SharePrefsRevisionRepositoryImpl
import ru.mirea.ivashechkinav.todo.di.components.AppContext

data class DatabaseConfig(val dbName: String)

@Module
interface LocalStorageModule {

    @Binds
    fun provideRevisionRepository(impl: SharePrefsRevisionRepositoryImpl): RevisionRepository

    companion object {
        @Provides
        fun provideDao(database: AppDatabase): TodoDao {
            return database.getTodoDao()
        }

        @Provides
        fun provideDatabase(@AppContext context: Context, config: DatabaseConfig): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                config.dbName
            )
                .fallbackToDestructiveMigration()
                .build()
        }

        @Provides
        fun provideDatabaseConfig(): DatabaseConfig {
            return DatabaseConfig(dbName = "todo.db")
        }
    }
}