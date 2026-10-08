package com.sonj.silosmonitoreo.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val contrasena: String,
    val rol: String
)

data class LoginResponse(
    val exito: Boolean,
    val mensaje: String,
    val token: String?,
    val usuarioNombre: String?,
    val rol: String?
)

data class SiloApiResponse(
    val id: String,
    val nombre: String,
    val porcentaje: Int,
    val tipoGrano: String,
    val capacidadToneladas: Int
)

interface SilosApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/silos")
    suspend fun obtenerSilos(
        @Header("Authorization") token: String
    ): Response<List<SiloApiResponse>>
}
