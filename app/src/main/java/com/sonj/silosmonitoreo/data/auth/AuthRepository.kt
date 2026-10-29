package com.sonj.silosmonitoreo.data.auth

import android.content.Context
import com.sonj.silosmonitoreo.data.db.LocalDatabaseManager
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.Usuario
import java.time.LocalDate
import java.util.UUID

object AuthRepository {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")

    var usuarioAutenticado: Usuario? = null
        private set

    fun esEmailValido(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    }

    fun validarLogin(
        context: Context,
        email: String,
        contrasena: String,
        rol: RolUsuario
    ): Pair<Boolean, String> {
        val emailTrimmed = email.trim()

        if (!esEmailValido(emailTrimmed)) {
            return Pair(false, "El formato de correo electrónico no es válido.")
        }

        if (contrasena.isBlank()) {
            return Pair(false, "Por favor ingresa tu contraseña.")
        }

        val db = LocalDatabaseManager.getInstance(context)
        val usuarioEncontrado = db.buscarUsuarioPorEmail(emailTrimmed)

        if (usuarioEncontrado == null) {
            return Pair(false, "El correo no está registrado en el sistema. Regístrate primero.")
        }

        if (usuarioEncontrado.contrasena != contrasena) {
            return Pair(false, "Contraseña incorrecta. Revisa tus datos.")
        }

        if (usuarioEncontrado.rol != rol) {
            return Pair(false, "El correo registrado pertenece al rol ${usuarioEncontrado.rol.etiqueta}, no a ${rol.etiqueta}.")
        }

        if (!usuarioEncontrado.esMayorDeEdad) {
            return Pair(false, "Acceso denegado: El usuario debe ser mayor de 18 años.")
        }

        val tokenGenerado = "Bearer_JWT_token_${UUID.randomUUID().toString().take(12)}"
        usuarioEncontrado.tokenSesion = tokenGenerado
        db.actualizarTokenUsuario(emailTrimmed, tokenGenerado)

        usuarioAutenticado = usuarioEncontrado
        return Pair(true, "Inicio de sesión exitoso. Token asignado.")
    }

    fun registrarNuevoUsuario(
        context: Context,
        nombre: String,
        email: String,
        contrasena: String,
        fechaNacimiento: LocalDate,
        rol: RolUsuario
    ): Pair<Boolean, String> {
        if (nombre.isBlank()) {
            return Pair(false, "Por favor ingresa tu nombre completo.")
        }

        val emailTrimmed = email.trim()
        if (!esEmailValido(emailTrimmed)) {
            return Pair(false, "Por favor ingresa un correo electrónico válido.")
        }

        if (contrasena.length < 6) {
            return Pair(false, "La contraseña debe tener al menos 6 caracteres.")
        }

        val usuarioPrueba = Usuario(
            id = UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            email = emailTrimmed,
            contrasena = contrasena,
            fechaNacimiento = fechaNacimiento,
            rol = rol
        )

        if (!usuarioPrueba.esMayorDeEdad) {
            return Pair(false, "Debes ser mayor de 18 años para registrarte (Edad actual: ${usuarioPrueba.edad} años).")
        }

        val db = LocalDatabaseManager.getInstance(context)
        val usuarioExistente = db.buscarUsuarioPorEmail(emailTrimmed)
        if (usuarioExistente != null) {
            return Pair(false, "Ya existe una cuenta registrada con el correo $emailTrimmed.")
        }

        val tokenNuevo = "Bearer_JWT_token_${UUID.randomUUID().toString().take(12)}"
        usuarioPrueba.tokenSesion = tokenNuevo

        val guardado = db.guardarUsuario(usuarioPrueba)
        if (guardado) {
            usuarioAutenticado = usuarioPrueba
            return Pair(true, "Cuenta creada exitosamente. Token asignado.")
        } else {
            return Pair(false, "Error al guardar el usuario en la base de datos.")
        }
    }

    fun cerrarSesion() {
        usuarioAutenticado = null
    }
}
