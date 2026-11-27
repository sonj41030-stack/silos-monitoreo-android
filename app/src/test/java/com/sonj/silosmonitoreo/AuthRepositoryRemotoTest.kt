package com.sonj.silosmonitoreo

import android.content.Context
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.data.api.MovimientosApi
import com.sonj.silosmonitoreo.data.api.SilosApi
import com.sonj.silosmonitoreo.data.api.TokenStore
import com.sonj.silosmonitoreo.data.auth.AuthRepository
import com.sonj.silosmonitoreo.data.authapi.AuthApiClient
import com.sonj.silosmonitoreo.data.authapi.AuthApiService
import com.sonj.silosmonitoreo.data.authapi.AuthLoginResponse
import com.sonj.silosmonitoreo.data.db.LocalDatabaseManager
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.Usuario
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate

class AuthRepositoryRemotoTest {

    private val auth = mockk<AuthApiService>()
    private val db = mockk<LocalDatabaseManager>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val silosApi = mockk<SilosApi>()
    private val movimientosApi = mockk<MovimientosApi>()

    private val loginOk = AuthLoginResponse(true, "Bienvenida", "tok123", "Ana Admin", "ADMINISTRADOR")

    @Before
    fun setUp() {
        mockkObject(AuthApiClient)
        every { AuthApiClient.service } returns auth
        mockkObject(LocalDatabaseManager.Companion)
        every { LocalDatabaseManager.getInstance(any()) } returns db
        every { db.buscarUsuarioPorEmail(any()) } returns null

        SilosRepository.silosApi = silosApi
        SilosRepository.movimientosApi = movimientosApi
        SilosRepository.limpiar()
        coEvery { silosApi.granjas() } returns Response.success(listOf(granjaDePrueba(siloDePrueba())))
        coEvery { movimientosApi.listar(any()) } returns Response.success(emptyList())
    }

    @After
    fun tearDown() {
        AuthRepository.cerrarSesion()
        unmockkAll()
    }

    private fun usuarioLocal(rol: RolUsuario = RolUsuario.OPERADOR, clave: String = "clave123", edad: Long = 30) =
        Usuario("u1", "Ana", "ana@silos.com", clave, LocalDate.now().minusYears(edad), rol)

    // ---------- Login remoto ----------

    @Test
    fun loginRemoto_exitoso_guarda_token_y_usuario() = runTest {
        coEvery { auth.login(any()) } returns Response.success(loginOk)

        val r = AuthRepository.loginRemoto(context, "admin@silos.com", "admin123", RolUsuario.ADMINISTRADOR)

        assertTrue(r.first)
        assertEquals("tok123", TokenStore.token)
        assertEquals(RolUsuario.ADMINISTRADOR, AuthRepository.usuarioAutenticado?.rol)
        assertEquals("Ana Admin", AuthRepository.usuarioAutenticado?.nombre)
        coVerify { silosApi.granjas() }
    }

    @Test
    fun loginRemoto_usa_la_fecha_guardada_localmente() = runTest {
        coEvery { auth.login(any()) } returns Response.success(loginOk)
        every { db.buscarUsuarioPorEmail("admin@silos.com") } returns usuarioLocal()

        AuthRepository.loginRemoto(context, "admin@silos.com", "admin123", RolUsuario.ADMINISTRADOR)

        assertEquals(LocalDate.now().minusYears(30), AuthRepository.usuarioAutenticado?.fechaNacimiento)
    }

    @Test
    fun loginRemoto_email_invalido_no_llama_a_la_api() = runTest {
        val r = AuthRepository.loginRemoto(context, "sin_arroba", "x", RolUsuario.OPERADOR)
        assertFalse(r.first)
        coVerify(exactly = 0) { auth.login(any()) }
    }

    @Test
    fun loginRemoto_contrasena_vacia_no_llama_a_la_api() = runTest {
        val r = AuthRepository.loginRemoto(context, "ana@silos.com", " ", RolUsuario.OPERADOR)
        assertFalse(r.first)
        coVerify(exactly = 0) { auth.login(any()) }
    }

    @Test
    fun loginRemoto_credenciales_incorrectas_muestra_mensaje() = runTest {
        coEvery { auth.login(any()) } returns errorHttp(401)
        val r = AuthRepository.loginRemoto(context, "ana@silos.com", "mala", RolUsuario.OPERADOR)
        assertFalse(r.first)
        assertTrue(r.second.contains("incorrectos"))
        assertNull(AuthRepository.usuarioAutenticado)
    }

    @Test
    fun loginRemoto_usa_el_mensaje_del_servidor() = runTest {
        coEvery { auth.login(any()) } returns errorHttp(403, """{"exito":false,"mensaje":"Rol no corresponde"}""")
        val r = AuthRepository.loginRemoto(context, "ana@silos.com", "x1", RolUsuario.OPERADOR)
        assertEquals("Rol no corresponde", r.second)
    }

    @Test
    fun loginRemoto_codigos_http_conocidos() = runTest {
        coEvery { auth.login(any()) } returns errorHttp(400)
        assertTrue(AuthRepository.loginRemoto(context, "ana@silos.com", "x1", RolUsuario.OPERADOR).second.contains("inválidos"))
        coEvery { auth.login(any()) } returns errorHttp(403)
        assertTrue(AuthRepository.loginRemoto(context, "ana@silos.com", "x1", RolUsuario.OPERADOR).second.contains("permiso"))
        coEvery { auth.login(any()) } returns errorHttp(500)
        assertTrue(AuthRepository.loginRemoto(context, "ana@silos.com", "x1", RolUsuario.OPERADOR).second.contains("500"))
    }

    @Test
    fun loginRemoto_sin_conexion() = runTest {
        coEvery { auth.login(any()) } throws IOException("sin red")
        val r = AuthRepository.loginRemoto(context, "ana@silos.com", "x1", RolUsuario.OPERADOR)
        assertFalse(r.first)
        assertTrue(r.second.contains("conectar"))
    }

    @Test
    fun cerrarSesion_limpia_usuario_token_y_datos() = runTest {
        coEvery { auth.login(any()) } returns Response.success(loginOk)
        AuthRepository.loginRemoto(context, "admin@silos.com", "admin123", RolUsuario.ADMINISTRADOR)

        AuthRepository.cerrarSesion()

        assertNull(AuthRepository.usuarioAutenticado)
        assertNull(TokenStore.token)
        assertTrue(SilosRepository.granjas.isEmpty())
    }

    // ---------- Registro remoto ----------

    private val nacimiento = LocalDate.now().minusYears(30)

    @Test
    fun registrarRemoto_exitoso_guarda_perfil_local_y_entra() = runTest {
        coEvery { auth.registro(any()) } returns Response.success(loginOk)
        coEvery { auth.login(any()) } returns Response.success(loginOk)
        every { db.guardarUsuario(any()) } returns true

        val r = AuthRepository.registrarRemoto(context, "Ana Perez", "ana@silos.com", "clave123", nacimiento, RolUsuario.ADMINISTRADOR)

        assertTrue(r.first)
        verify { db.guardarUsuario(any()) }
        assertEquals("tok123", TokenStore.token)
    }

    @Test
    fun registrarRemoto_valida_antes_de_llamar_a_la_api() = runTest {
        assertFalse(AuthRepository.registrarRemoto(context, " ", "ana@silos.com", "clave123", nacimiento, RolUsuario.OPERADOR).first)
        assertFalse(AuthRepository.registrarRemoto(context, "Ana", "mal", "clave123", nacimiento, RolUsuario.OPERADOR).first)
        assertFalse(AuthRepository.registrarRemoto(context, "Ana", "ana@silos.com", "123", nacimiento, RolUsuario.OPERADOR).first)
        assertFalse(AuthRepository.registrarRemoto(context, "Ana", "ana@silos.com", "clave123", LocalDate.now().minusYears(15), RolUsuario.OPERADOR).first)
        coVerify(exactly = 0) { auth.registro(any()) }
    }

    @Test
    fun registrarRemoto_correo_duplicado() = runTest {
        coEvery { auth.registro(any()) } returns errorHttp(409)
        val r = AuthRepository.registrarRemoto(context, "Ana Perez", "ana@silos.com", "clave123", nacimiento, RolUsuario.OPERADOR)
        assertFalse(r.first)
        assertTrue(r.second.contains("Ya existe"))
    }

    @Test
    fun registrarRemoto_sin_conexion() = runTest {
        coEvery { auth.registro(any()) } throws IOException("sin red")
        val r = AuthRepository.registrarRemoto(context, "Ana Perez", "ana@silos.com", "clave123", nacimiento, RolUsuario.OPERADOR)
        assertTrue(r.second.contains("conectar"))
    }

    // ---------- Modo local (SQLite) ----------

    @Test
    fun validarLogin_exitoso() {
        every { db.buscarUsuarioPorEmail("ana@silos.com") } returns usuarioLocal()
        val r = AuthRepository.validarLogin(context, "ana@silos.com", "clave123", RolUsuario.OPERADOR)
        assertTrue(r.first)
        verify { db.actualizarTokenUsuario("ana@silos.com", any()) }
    }

    @Test
    fun validarLogin_rechaza_datos_incorrectos() {
        every { db.buscarUsuarioPorEmail("ana@silos.com") } returns usuarioLocal()
        assertFalse(AuthRepository.validarLogin(context, "mal", "clave123", RolUsuario.OPERADOR).first)
        assertFalse(AuthRepository.validarLogin(context, "ana@silos.com", "", RolUsuario.OPERADOR).first)
        assertFalse(AuthRepository.validarLogin(context, "ana@silos.com", "otra", RolUsuario.OPERADOR).first)
        assertFalse(AuthRepository.validarLogin(context, "ana@silos.com", "clave123", RolUsuario.JEFATURA).first)
        assertFalse(AuthRepository.validarLogin(context, "nadie@silos.com", "clave123", RolUsuario.OPERADOR).first)
    }

    @Test
    fun validarLogin_rechaza_menores_de_edad() {
        every { db.buscarUsuarioPorEmail("ana@silos.com") } returns usuarioLocal(edad = 16)
        assertFalse(AuthRepository.validarLogin(context, "ana@silos.com", "clave123", RolUsuario.OPERADOR).first)
    }

    @Test
    fun registrarNuevoUsuario_exitoso() {
        every { db.guardarUsuario(any()) } returns true
        val r = AuthRepository.registrarNuevoUsuario(context, "Ana Perez", "ana@silos.com", "clave123", nacimiento, RolUsuario.OPERADOR)
        assertTrue(r.first)
        assertEquals("ana@silos.com", AuthRepository.usuarioAutenticado?.email)
    }

    @Test
    fun registrarNuevoUsuario_rechaza_datos_invalidos_y_duplicados() {
        assertFalse(AuthRepository.registrarNuevoUsuario(context, "", "ana@silos.com", "clave123", nacimiento, RolUsuario.OPERADOR).first)
        assertFalse(AuthRepository.registrarNuevoUsuario(context, "Ana", "mal", "clave123", nacimiento, RolUsuario.OPERADOR).first)
        assertFalse(AuthRepository.registrarNuevoUsuario(context, "Ana", "ana@silos.com", "1", nacimiento, RolUsuario.OPERADOR).first)
        assertFalse(AuthRepository.registrarNuevoUsuario(context, "Ana", "ana@silos.com", "clave123", LocalDate.now().minusYears(10), RolUsuario.OPERADOR).first)

        every { db.buscarUsuarioPorEmail("ana@silos.com") } returns usuarioLocal()
        assertFalse(AuthRepository.registrarNuevoUsuario(context, "Ana", "ana@silos.com", "clave123", nacimiento, RolUsuario.OPERADOR).first)
    }

    @Test
    fun registrarNuevoUsuario_falla_si_no_se_puede_guardar() {
        every { db.guardarUsuario(any()) } returns false
        val r = AuthRepository.registrarNuevoUsuario(context, "Ana Perez", "ana@silos.com", "clave123", nacimiento, RolUsuario.OPERADOR)
        assertFalse(r.first)
    }
}