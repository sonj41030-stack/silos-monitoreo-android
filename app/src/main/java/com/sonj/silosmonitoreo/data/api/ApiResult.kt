package com.sonj.silosmonitoreo.data.api

import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

sealed class Resultado<out T> {
    data class Ok<out T>(val data: T) : Resultado<T>()
    data class Error(val mensaje: String) : Resultado<Nothing>()
}

/** Ejecuta una llamada Retrofit y la convierte en Ok/Error con un mensaje legible. */
suspend fun <T> llamarApi(block: suspend () -> Response<T>): Resultado<T> =
    try {
        val respuesta = block()
        val cuerpo = respuesta.body()
        when {
            respuesta.isSuccessful && cuerpo != null -> Resultado.Ok(cuerpo)
            respuesta.isSuccessful -> Resultado.Error("Respuesta vacía del servidor.")
            else -> Resultado.Error(respuesta.mensajeError())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Resultado.Error("No se pudo conectar con el servidor. Verifica que los microservicios estén encendidos.")
    } catch (e: Exception) {
        Resultado.Error("Error inesperado: ${e.message}")
    }

/** Extrae el mensaje del ApiError del backend; si no se puede, usa el código HTTP. */
fun <T> Response<T>.mensajeError(): String {
    val texto = try { errorBody()?.string() } catch (e: Exception) { null }
    val delBackend = try {
        val json = JsonParser.parseString(texto).asJsonObject
        listOf("message", "mensaje", "error")
            .firstNotNullOfOrNull { k -> json.get(k)?.takeIf { it.isJsonPrimitive }?.asString }
    } catch (e: Exception) {
        null
    }
    return delBackend ?: when (code()) {
        401 -> "Sesión expirada o credenciales inválidas."
        403 -> "No tienes permisos para esta acción."
        else -> "Error del servidor (${code()})."
    }
}
