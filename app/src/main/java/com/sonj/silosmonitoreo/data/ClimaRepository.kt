package com.sonj.silosmonitoreo.data

import com.sonj.silosmonitoreo.data.api.ApiClient
import com.sonj.silosmonitoreo.data.api.ApiConfig
import com.sonj.silosmonitoreo.data.api.ClimaApi
import com.sonj.silosmonitoreo.data.api.Resultado
import com.sonj.silosmonitoreo.data.api.llamarApi

data class Clima(val temperaturaC: Double, val humedadPct: Int)

/** API externa: la humedad y temperatura ambiente importan para conservar el grano. */
object ClimaRepository {
    var api: ClimaApi = ApiClient.climaApi

    suspend fun obtener(
        latitud: Double = ApiConfig.CLIMA_LATITUD,
        longitud: Double = ApiConfig.CLIMA_LONGITUD
    ): Resultado<Clima> =
        when (val r = llamarApi { api.clima(latitud, longitud) }) {
            is Resultado.Ok -> Resultado.Ok(Clima(r.data.current.temperatura, r.data.current.humedad))
            is Resultado.Error -> r
        }
}
