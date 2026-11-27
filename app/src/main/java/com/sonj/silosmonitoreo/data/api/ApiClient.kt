package com.sonj.silosmonitoreo.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Cliente para NUESTROS microservicios: agrega "Authorization: Bearer <jwt>" a cada petición.
    private val clienteConAuth: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = TokenStore.token
                val request = if (token.isNullOrBlank()) {
                    chain.request()
                } else {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                }
                chain.proceed(request)
            }
            // BASIC: solo método, URL y código. No registra cuerpos (hay contraseñas y tokens).
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // Cliente para la API EXTERNA: sin interceptor, para NO filtrar nuestro JWT a un tercero.
    private val clienteExterno: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val authApi: AuthApi by lazy { retrofit(ApiConfig.AUTH_URL, clienteConAuth).create(AuthApi::class.java) }
    val silosApi: SilosApi by lazy { retrofit(ApiConfig.SILOS_URL, clienteConAuth).create(SilosApi::class.java) }
    val movimientosApi: MovimientosApi by lazy { retrofit(ApiConfig.MOVIMIENTOS_URL, clienteConAuth).create(MovimientosApi::class.java) }
    val climaApi: ClimaApi by lazy { retrofit(ApiConfig.CLIMA_URL, clienteExterno).create(ClimaApi::class.java) }
}
