package ru.mirea.ivashechkinav.todo.data.retrofit

import retrofit2.http.*
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NetworkRequest
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NetworkRequestList
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NetworkResponse
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NetworkResponseList

interface TodoApi {
    @GET("list")
    suspend fun getAll(): NetworkResponseList

    @GET("list/{id}")
    suspend fun getByID(
        @Path("id") id: String
    ): NetworkResponse

    @POST("list")
    suspend fun add(
        @Body itemRequest: NetworkRequest
    ): NetworkResponse

    @PUT("list/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body itemRequest: NetworkRequest
    ): NetworkResponse

    @DELETE("list/{id}")
    suspend fun delete(
        @Path("id") id: String,
    ): NetworkResponse

    @PATCH("list")
    suspend fun patch(
        @Body listRequest: NetworkRequestList
    ): NetworkResponseList
}