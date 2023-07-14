package ru.mirea.ivashechkinav.todo.data.retrofit.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import ru.mirea.ivashechkinav.todo.di.components.AppScope
import ru.mirea.ivashechkinav.todo.di.modules.TokenInterceptorConfig
import javax.inject.Inject

@AppScope
class TokenInterceptor @Inject constructor(private val interceptorConfig: TokenInterceptorConfig) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequest = request.newBuilder()
            .addHeader(interceptorConfig.apiKeyName, interceptorConfig.apiKeyValue).build()
        return chain.proceed(newRequest)
    }
}