package com.sonj.silosmonitoreo.data.api

object ApiConfig {
    // 10.0.2.2 = tu PC visto desde el emulador de Android.
    // En un teléfono físico usa la IP de tu PC en el WiFi (ej. "192.168.1.50").
    private const val HOST = "10.0.2.2"

    const val AUTH_URL = "http://$HOST:8081/"
    const val SILOS_URL = "http://$HOST:8082/"
    const val MOVIMIENTOS_URL = "http://$HOST:8083/"

    // API externa (Open-Meteo: gratis, sin API key)
    const val CLIMA_URL = "https://api.open-meteo.com/"
    const val CLIMA_LATITUD = -33.45
    const val CLIMA_LONGITUD = -70.66
}

/** Guarda el JWT que entrega ms-auth. El interceptor lo agrega a cada petición. */
object TokenStore {
    @Volatile
    var token: String? = null
}
