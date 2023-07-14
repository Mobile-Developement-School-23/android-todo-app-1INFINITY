package ru.mirea.ivashechkinav.todo.di.modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import ru.mirea.ivashechkinav.todo.data.retrofit.TodoApi
import ru.mirea.ivashechkinav.todo.data.retrofit.interceptors.RevisionInterceptor
import ru.mirea.ivashechkinav.todo.data.retrofit.interceptors.TokenInterceptor
import ru.mirea.ivashechkinav.todo.di.components.AppScope

data class RetrofitConfig(val baseUrl: String)
data class TokenInterceptorConfig(val apiKeyName: String, val apiKeyValue: String)
data class ApiToken(val token: String)

@Module
interface RemoteStorageModule {
    companion object {

        @AppScope
        @Provides
        fun provideApi(retrofit: Retrofit): TodoApi {
            return retrofit.create(TodoApi::class.java)
        }

        @AppScope
        @Provides
        fun provideRetrofit(
            client: OkHttpClient,
            converterFactory: Converter.Factory,
            config: RetrofitConfig
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(config.baseUrl)
                .addConverterFactory(converterFactory)
                .client(client)
                .build()
        }

        @AppScope
        @Provides
        fun provideOkHttpClient(
            tokenInterceptor: TokenInterceptor,
            revisionInterceptor: RevisionInterceptor
        ): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor)
                .addInterceptor(revisionInterceptor)
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level =
                            HttpLoggingInterceptor.Level.BODY // Уровень логгирования (BODY, BASIC, HEADERS)
                    })
                .build()
        }

        @AppScope
        @Provides
        fun provideConverterFactory(): Converter.Factory {
            return Json.asConverterFactory("application/json".toMediaType())
        }

        @AppScope
        @Provides
        fun provideRetrofitConfig(): RetrofitConfig {
            return RetrofitConfig(baseUrl = "https://beta.mrdekk.ru/todobackend/")
        }

        @AppScope
        @Provides
        fun provideInterceptorConfig(apiToken: ApiToken): TokenInterceptorConfig {
            return TokenInterceptorConfig(
                apiKeyName = "Authorization",
                apiKeyValue = "Bearer ${apiToken.token}"
            )
        }

        @AppScope
        @Provides
        fun provideApiToken(): ApiToken {
            return ApiToken(token = "sanjakate")
        }
    }
}