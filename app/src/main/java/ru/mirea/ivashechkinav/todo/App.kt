package ru.mirea.ivashechkinav.todo

import android.app.Application
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl
import ru.mirea.ivashechkinav.todo.data.retrofit.TodoApi
import ru.mirea.ivashechkinav.todo.data.room.AppDatabase
import ru.mirea.ivashechkinav.todo.data.room.TodoDao
import ru.mirea.ivashechkinav.todo.data.sharedprefs.SharePrefsRevisionRepositoryImpl
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository

class App : Application() {

    lateinit var repository: TodoItemsRepository

    override fun onCreate() {
        super.onCreate()
        val todoDao = provideDao()
        val api = provideRetrofitApi()
        val revisionRepository = SharePrefsRevisionRepositoryImpl(this.applicationContext)
//        GlobalScope.launch(Dispatchers.IO) {
//
//            try{
//                val result = api.getAll()
//                result
//            } catch (e: Exception) {
//                e
//            }
//
//        }
        repository = TodoItemsRepositoryImpl(todoDao, api, revisionRepository)
    }

    private fun provideDao(): TodoDao {
        return Room.databaseBuilder(
            this.applicationContext,
            AppDatabase::class.java,
            DB_NAME
        )
            .fallbackToDestructiveMigration()
            .build().getTodoDao()
    }
    private fun provideRetrofitApi(): TodoApi {
        val okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor{ chain ->
                val request = chain.request().newBuilder().addHeader(API_KEY_NAME, API_KEY_VALUE).build()
                return@addInterceptor chain.proceed(request)
            }
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)
            .build()

        return retrofit.create(TodoApi::class.java)
    }

    private companion object {
        const val DB_NAME = "todo.db"

        const val BASE_URL = "https://beta.mrdekk.ru/todobackend/"
        const val API_KEY_NAME = "Authorization"
        const val API_KEY_VALUE = "Bearer sanjakate"
    }
}