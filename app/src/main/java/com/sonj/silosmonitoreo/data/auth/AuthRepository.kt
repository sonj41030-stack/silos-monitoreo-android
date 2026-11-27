package com.sonj.silosmonitoreo.data.auth

import android.content.Context
import com.google.gson.Gson
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.data.authapi.AuthApiClient
import com.sonj.silosmonitoreo.data.authapi.AuthLoginRequest
import com.sonj.silosmonitoreo.data.authapi.AuthLoginResponse
import com.sonj.silosmonitoreo.data.authapi.AuthRegistroRequest
import com.sonj.silosmonitoreo.data.authapi.AuthSession
import com.sonj.silosmonitoreo.data.db.LocalDatabaseManager
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import java.util.UUID

object AuthRepository {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")

    var usuarioAutenticado: Usuario? = null
        private set

    fun esEmailValido(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    }

    // ---------------- Modo local (SQLite), se conserva ----------------

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
            ?: return Pair(false, "El correo no está registrado en el sistema. Regístrate primero.")

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
        if (db.buscarUsuarioPorEmail(emailTrimmed) != null) {
            return Pair(false, "Ya existe una cuenta registrada con el correo $emailTrimmed.")
        }

        usuarioPrueba.tokenSesion = "Bearer_JWT_token_${UUID.randomUUID().toString().take(12)}"
        return if (db.guardarUsuario(usuarioPrueba)) {
            usuarioAutenticado = usuarioPrueba
            Pair(true, "Cuenta creada exitosamente. Token asignado.")
        } else {
            Pair(false, "Error al guardar el usuario en la base de datos.")
        }
    }

    // ---------------- Modo remoto (auth-service) ----------------

    private const val MSG_SIN_CONEXION =
        "No se pudo conectar con el servidor. Verifica que el auth-service esté encendido."

    private fun extraerMensaje(resp: retrofit2.Response<*>): String {
        val cuerpo = try { resp.errorBody()?.string() } catch (e: Exception) { null }
        val msg = try { Gson().fromJson(cuerpo, AuthLoginResponse::class.java)?.mensaje } catch (e: Exception) { null }
        if (!msg.isNullOrBlank()) return msg
        return when (resp.code()) {
            400 -> "Datos inválidos. Revisa el formulario."
            401 -> "Correo o contraseña incorrectos."
            403 -> "No tienes permiso para esta acción."
            409 -> "Ya existe una cuenta con ese correo."
            else -> "Error del servidor (código ${resp.code()})."
        }
    }

    suspend fun loginRemoto(
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
        return try {
            val resp = withContext(Dispatchers.IO) {
                AuthApiClient.service.login(AuthLoginRequest(emailTrimmed, contrasena, rol.name))
            }
            val body = resp.body()
            if (resp.isSuccessful && body != null && body.exito && !body.token.isNullOrBlank()) {
                val rolReal = RolUsuario.entries.firstOrNull { it.name == body.rol } ?: rol
                val local: Usuario? = withContext(Dispatchers.IO) {
                    LocalDatabaseManager.getInstance(context).buscarUsuarioPorEmail(emailTrimmed)
                }
                AuthSession.token = body.token
                SilosRepository.cargarGranjas()
                SilosRepository.cargarMovimientos()
                usuarioAutenticado = Usuario(
                    id = local?.id ?: UUID.randomUUID().toString(),
                    nombre = body.usuarioNombre ?: emailTrimmed,
                    email = emailTrimmed,
                    contrasena = "",
                    fechaNacimiento = local?.fechaNacimiento ?: LocalDate.of(2000, 1, 1),
                    rol = rolReal,
                    tokenSesion = body.token
                )
                Pair(true, body.mensaje ?: "Inicio de sesión exitoso")
            } else {
                Pair(false, extraerMensaje(resp))
            }
        } catch (e: IOException) {
            Pair(false, MSG_SIN_CONEXION)
        }
    }

    suspend fun registrarRemoto(
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
        val usuarioLocal = Usuario(
            id = UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            email = emailTrimmed,
            contrasena = "",
            fechaNacimiento = fechaNacimiento,
            rol = rol
        )
        if (!usuarioLocal.esMayorDeEdad) {
            return Pair(false, "Debes ser mayor de 18 años para registrarte (Edad actual: ${usuarioLocal.edad} años).")
        }
        return try {
            val resp = withContext(Dispatchers.IO) {
                AuthApiClient.service.registro(AuthRegistroRequest(usuarioLocal.nombre, emailTrimmed, contrasena, fechaNacimiento.toString(), rol.name))
            }
            if (resp.isSuccessful) {
                // Fecha de nacimiento y perfil quedan en SQLite (el servidor no los maneja)
                withContext(Dispatchers.IO) {
                    val db = LocalDatabaseManager.getInstance(context)
                    if (db.buscarUsuarioPorEmail(emailTrimmed) == null) db.guardarUsuario(usuarioLocal)
                }
                loginRemoto(context, emailTrimmed, contrasena, rol)
            } else {
                Pair(false, extraerMensaje(resp))
            }
        } catch (e: IOException) {
            Pair(false, MSG_SIN_CONEXION)
        }
    }

    fun cerrarSesion() {
        usuarioAutenticado = null
        AuthSession.limpiar()
        SilosRepository.limpiar()
    }
}