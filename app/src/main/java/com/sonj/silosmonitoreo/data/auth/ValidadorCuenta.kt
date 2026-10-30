package com.sonj.silosmonitoreo.data.auth

import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period

/**
 * Reglas de validación del formulario "Crear nueva cuenta".
 * Cada función devuelve el mensaje de error del campo, o null si es válido.
 */
object ValidadorCuenta {

    fun errorNombre(nombre: String): String? = when {
        nombre.isBlank() -> "Ingresa tu nombre completo."
        nombre.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres."
        else -> null
    }

    // El formato del correo lo valida AuthRepository.esEmailValido (Regex)
    fun errorEmail(email: String): String? = when {
        email.isBlank() -> "Ingresa tu correo electrónico."
        !AuthRepository.esEmailValido(email) ->
            "Correo inválido: debe incluir @ y dominio (ej. usuario@silos.com)."
        else -> null
    }

    fun errorContrasena(contrasena: String): String? =
        if (contrasena.length < 6) "La contraseña debe tener al menos 6 caracteres." else null

    fun errorFechaNacimiento(dia: String, mes: String, anio: String): String? {
        val d = dia.trim().toIntOrNull()
        val m = mes.trim().toIntOrNull()
        val a = anio.trim().toIntOrNull()
        if (d == null || m == null || a == null) return "Ingresa día, mes y año con números."

        val hoy = LocalDate.now()
        if (a !in 1900..hoy.year) return "Ingresa un año válido (1900 - ${hoy.year})."

        val fecha = try {
            LocalDate.of(a, m, d)
        } catch (e: DateTimeException) {
            return "La fecha ingresada no existe."
        }
        if (fecha.isAfter(hoy)) return "La fecha de nacimiento no puede ser futura."

        val edad = Period.between(fecha, hoy).years
        return if (edad < 18) "Debes ser mayor de 18 años (edad actual: $edad años)." else null
    }
}
