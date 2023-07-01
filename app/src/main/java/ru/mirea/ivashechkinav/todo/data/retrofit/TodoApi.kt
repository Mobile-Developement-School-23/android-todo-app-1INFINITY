package ru.mirea.ivashechkinav.todo.data.retrofit

import retrofit2.http.*

interface TodoApi {
    @GET("list")
    suspend fun getAll(): NWResponseList

    @GET("list/{id}")
    suspend fun getByID(
        @Path("id") id: String
    ): NWResponse

    @POST("list")
    suspend fun add(
        @Header(LAST_REVISION) revision: Int,
        @Body itemRequest: NWRequest
    ): NWResponse

    @PUT("list/{id}")
    suspend fun update(
        @Header(LAST_REVISION) revision: Int,
        @Path("id") id: String,
        @Body itemRequest: NWRequest
    ): NWResponse

    @DELETE("list/{id}")
    suspend fun delete(
        @Header(LAST_REVISION) revision: Int,
        @Path("id") id: String,
    ): NWResponse

    @PATCH("list")
    suspend fun patch(
        @Header(LAST_REVISION) revision: Int,
        @Body listRequest: NWRequestList
    ): NWResponseList

    companion object {
        const val LAST_REVISION = "X-Last-Known-Revision"
    }
}