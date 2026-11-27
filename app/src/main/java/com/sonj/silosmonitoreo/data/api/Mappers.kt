package com.sonj.silosmonitoreo.data.api

import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.Movimiento
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.model.TipoMovimiento
import com.sonj.silosmonitoreo.model.Usuario
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

// Backend trabaja en kg/Double; la app en toneladas/Int.

fun SiloApiResponse.toModel() = Silo(
    id = id,
    nombre = nombre,
    porcentaje = porcentaje.roundToInt(),
    tipoGrano = producto,
    capacidadToneladas = capacidadTn.roundToInt(),
    toneladasActuales = toneladasActuales.roundToInt()
)

// AJUSTA los parámetros si tu clase Granja tiene otros campos.
fun GranjaApiResponse.toModel() = Granja(
    id = id,
    nombre = nombre,
    ubicacion = ubicacion ?: "",
    silos = silos.map { it.toModel() }
)

fun MovimientoApiResponse.toModel(siloNombre: String, granjaNombre: String) = Movimiento(
    id = id,
    siloId = siloId,
    siloNombre = siloNombre,
    granjaNombre = granjaNombre,
    tipo = TipoMovimiento.valueOf(tipo),
    cantidadToneladas = (cantidadKg / 1000.0).roundToInt(),
    fechaHora = LocalDateTime.parse(fechaHora),
    responsable = usuarioEmail,
    notas = observacion ?: ""
)

fun UsuarioApiResponse.toUsuario(token: String) = Usuario(
    id = id,
    nombre = nombre,
    email = email,
    contrasena = "", // la contraseña nunca se guarda en el teléfono
    fechaNacimiento = LocalDate.parse(fechaNacimiento),
    rol = RolUsuario.valueOf(rol),
    tokenSesion = token
)
