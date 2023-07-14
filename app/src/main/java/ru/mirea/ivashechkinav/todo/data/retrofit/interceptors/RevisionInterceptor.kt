package ru.mirea.ivashechkinav.todo.data.retrofit.interceptors

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.Response
import ru.mirea.ivashechkinav.todo.data.sharedprefs.SharePrefsRevisionRepository
import ru.mirea.ivashechkinav.todo.di.components.AppScope
import javax.inject.Inject

@AppScope
class RevisionInterceptor @Inject constructor(
    private val revisionRepo: SharePrefsRevisionRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (request.method in methodsWithRevisionRequired) {
            val lastRevision = revisionRepo.getLastRevision().toString()
            request = request.newBuilder()
                .addHeader("X-Last-Known-Revision", lastRevision).build()
        }

        val response = chain.proceed(request)
        if (response.isSuccessful) {
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            val revision = extractRevisionFromJson(responseBody)
            revision?.let {
                revisionRepo.setRevision(it)
            }
        }
        return response
    }

    private fun extractRevisionFromJson(json: String?): Int? {
        val jsonElement = Json.parseToJsonElement(json ?: "")

        if (jsonElement is JsonObject) {
            return jsonElement[REVISION_FIELD]?.jsonPrimitive?.content?.toIntOrNull()
        }

        return null
    }

    companion object {
        const val REVISION_FIELD = "revision"
        val methodsWithRevisionRequired = listOf("POST", "PUT", "PATCH", "DELETE")
    }
}