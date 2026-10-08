package com.sonj.silosmonitoreo.model

import java.time.LocalDate
import java.time.Period

data class Usuario(
    val id: String,
    val nombre: String,
    val email: String,
    val contrasena: String,
    val fechaNacimiento: LocalDate,
    val rol: RolUsuario,
    var tokenSesion: String? = null
) {
    val edad: Int
        get() = Period.between(fechaNacimiento, LocalDate.now()).years

    val esMayorDeEdad: Boolean
        get() = edad >= 18
}
