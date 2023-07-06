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

data class RetrofitConfig(val baseUrl: String)
data class InterceptorConfig(val apiKeyName: String, val apiKeyValue: String)

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
        fun provideOkHttpClient(interceptorConfig: InterceptorConfig): OkHttpClient {
            return OkHttpClient.Builder()
                .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor { chain ->
                    val request =
                        chain.request().newBuilder()
                            .addHeader(interceptorConfig.apiKeyName, interceptorConfig.apiKeyValue)
                            .build()
                    return@addInterceptor chain.proceed(request)
                }
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
        fun provideInterceptorConfig(): InterceptorConfig {
            return InterceptorConfig(apiKeyName = "Authorization", apiKeyValue = "Bearer sanjakate")
        }
    }
}