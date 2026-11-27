package com.sonj.silosmonitoreo.data.authapi

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class AuthLoginRequest(
    val email: String,
    val contrasena: String,
    val rol: String
)

data class AuthLoginResponse(
    val exito: Boolean,
    val mensaje: String?,
    val token: String?,
    val usuarioNombre: String?,
    val rol: String?
)

data class AuthRegistroRequest(
    val nombre: String,
    val email: String,
    val contrasena: String,
    val rol: String
)

data class AuthUsuarioResponse(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String
)

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: AuthLoginRequest): Response<AuthLoginResponse>

    @POST("api/auth/registro")
    suspend fun registro(@Body request: AuthRegistroRequest): Response<AuthUsuarioResponse>
}

/** Token JWT de la sesion actual. */
object AuthSession {
    @Volatile
    var token: String? = null

    fun limpiar() {
        token = null
    }
}

object AuthApiClient {
    // 10.0.2.2 = tu PC desde el emulador. En celular fisico usa la IP de tu PC.
    const val BASE_URL = "http://10.0.2.2:8081/"

    val service: AuthApiService by lazy {
        val http = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}