package com.sonj.silosmonitoreo.data.api

import com.google.gson.annotations.SerializedName

// ── ms-auth ──────────────────────────────────────────────────────────
data class LoginRequest(val email: String, val contrasena: String, val rol: String)

data class LoginResponse(
    val exito: Boolean,
    val mensaje: String,
    val token: String?,
    val usuarioNombre: String?,
    val rol: String?
)

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val contrasena: String,
    val fechaNacimiento: String, // yyyy-MM-dd
    val rol: String
)

data class UsuarioApiResponse(
    val id: String,
    val nombre: String,
    val email: String,
    val fechaNacimiento: String,
    val rol: String
)

// ── ms-silos ─────────────────────────────────────────────────────────
data class SiloApiResponse(
    val id: String,
    val nombre: String,
    val producto: String,
    val capacidadTn: Double,
    val toneladasActuales: Double,
    val porcentaje: Double,
    val estado: String,
    val granjaId: String,
    val granjaNombre: String
)

data class GranjaApiResponse(
    val id: String,
    val nombre: String,
    val ubicacion: String?,
    val silos: List<SiloApiResponse>
)

data class AjusteSiloRequest(val tipo: String, val cantidadKg: Double)

// ── ms-movimientos ───────────────────────────────────────────────────
data class MovimientoApiRequest(
    val siloId: String,
    val tipo: String,
    val cantidadKg: Double,
    val observacion: String?
)

data class MovimientoApiResponse(
    val id: String,
    val siloId: String,
    val tipo: String,
    val cantidadKg: Double,
    val fechaHora: String,
    val usuarioEmail: String,
    val observacion: String?
)

// ── API externa: Open-Meteo ──────────────────────────────────────────
data class ClimaResponse(val current: ClimaActual)

data class ClimaActual(
    @SerializedName("temperature_2m") val temperatura: Double,
    @SerializedName("relative_humidity_2m") val humedad: Int
)

// Cuerpo de POST/PUT /api/silos (solo administrador)
data class SiloApiRequest(
    val nombre: String,
    val producto: String,
    val capacidadTn: Double,
    val toneladasActuales: Double,
    val granjaId: String
)