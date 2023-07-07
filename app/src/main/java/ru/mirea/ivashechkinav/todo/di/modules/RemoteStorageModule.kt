package ru.mirea.ivashechkinav.todo.di.modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import ru.mirea.ivashechkinav.todo.data.retrofit.TodoApi
import ru.mirea.ivashechkinav.todo.data.retrofit.interceptors.RevisionInterceptor
import ru.mirea.ivashechkinav.todo.data.retrofit.interceptors.TokenInterceptor

data class RetrofitConfig(val baseUrl: String)
data class TokenInterceptorConfig(val apiKeyName: String, val apiKeyValue: String)
data class ApiToken(val token: String)

@Module
interface RemoteStorageModule {
    companion object {
        @Provides
        fun provideApi(retrofit: Retrofit): TodoApi {
            return retrofit.create(TodoApi::class.java)
        }

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

        @Provides
        fun provideOkHttpClient(
            tokenInterceptor: TokenInterceptor,
            revisionInterceptor: RevisionInterceptor
        ): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor)
                .addInterceptor(revisionInterceptor)
                .build()
        }

        @Provides
        fun provideConverterFactory(): Converter.Factory {
            return Json.asConverterFactory("application/json".toMediaType())
        }

        @Provides
        fun provideRetrofitConfig(): RetrofitConfig {
            return RetrofitConfig(baseUrl = "https://beta.mrdekk.ru/todobackend/")
        }

        @Provides
        fun provideInterceptorConfig(apiToken: ApiToken): TokenInterceptorConfig {
            return TokenInterceptorConfig(
                apiKeyName = "Authorization",
                apiKeyValue = "Bearer ${apiToken.token}"
            )
        }

        @Provides
        fun provideApiToken(): ApiToken {
            return ApiToken(token = "sanjakate")
        }
    }
}