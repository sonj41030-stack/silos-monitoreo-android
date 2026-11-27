package com.sonj.silosmonitoreo

import com.sonj.silosmonitoreo.data.api.GranjaApiResponse
import com.sonj.silosmonitoreo.data.api.MovimientoApiResponse
import com.sonj.silosmonitoreo.data.api.SiloApiResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

fun siloDePrueba(
    id: String = "s1",
    nombre: String = "Silo A1",
    capacidad: Double = 100.0,
    actuales: Double = 50.0
) = SiloApiResponse(id, nombre, "Trigo", capacidad, actuales, actuales / capacidad * 100, "NORMAL", "g1", "Granja Uno")

fun granjaDePrueba(vararg silos: SiloApiResponse) =
    GranjaApiResponse("g1", "Granja Uno", "Melipilla", silos.toList())

fun movimientoDePrueba(siloId: String = "s1", tipo: String = "ENTRADA") =
    MovimientoApiResponse("m1", siloId, tipo, 10000.0, "2026-01-15T10:30:00", "admin@silos.com", "prueba")

fun <T> errorHttp(codigo: Int, json: String = ""): Response<T> =
    Response.error(codigo, json.toResponseBody("application/json".toMediaType()))