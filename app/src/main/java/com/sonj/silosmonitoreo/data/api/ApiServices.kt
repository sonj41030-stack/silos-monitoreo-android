package com.sonj.silosmonitoreo.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Microservicio ms-auth (puerto 8081) */
interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>

    @GET("api/auth/me")
    suspend fun me(): Response<UsuarioApiResponse>
}

/** Microservicio ms-silos (puerto 8082) */
interface SilosApi {
    @GET("api/granjas")
    suspend fun granjas(): Response<List<GranjaApiResponse>>

    @POST("api/silos")
    suspend fun crearSilo(@Body request: SiloApiRequest): Response<SiloApiResponse>

    @PUT("api/silos/{id}")
    suspend fun actualizarSilo(
        @Path("id") siloId: String,
        @Body request: SiloApiRequest
    ): Response<SiloApiResponse>

    @DELETE("api/silos/{id}")
    suspend fun eliminarSilo(@Path("id") siloId: String): Response<Unit>

    @POST("api/silos/{id}/movimientos")
    suspend fun ajustar(
        @Path("id") siloId: String,
        @Body request: AjusteSiloRequest
    ): Response<SiloApiResponse>
}

/** Microservicio ms-movimientos (puerto 8083) */
interface MovimientosApi {
    @POST("api/movimientos")
    suspend fun registrar(@Body request: MovimientoApiRequest): Response<MovimientoApiResponse>

    @GET("api/movimientos")
    suspend fun listar(@Query("siloId") siloId: String? = null): Response<List<MovimientoApiResponse>>
}

/** API EXTERNA (Open-Meteo). No es nuestra: sin JWT y por HTTPS. */
interface ClimaApi {
    @GET("v1/forecast")
    suspend fun clima(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m"
    ): Response<ClimaResponse>
}