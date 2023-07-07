package ru.mirea.ivashechkinav.todo.data.retrofit

import retrofit2.http.*
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NWRequest
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NWRequestList
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NWResponse
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NWResponseList

interface TodoApi {
    @GET("list")
    suspend fun getAll(): NWResponseList

    @GET("list/{id}")
    suspend fun getByID(
        @Path("id") id: String
    ): NWResponse

    @POST("list")
    suspend fun add(
        @Body itemRequest: NWRequest
    ): NWResponse

    @PUT("list/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body itemRequest: NWRequest
    ): NWResponse

    @DELETE("list/{id}")
    suspend fun delete(
        @Path("id") id: String,
    ): NWResponse

    @PATCH("list")
    suspend fun patch(
        @Body listRequest: NWRequestList
    ): NWResponseList
}