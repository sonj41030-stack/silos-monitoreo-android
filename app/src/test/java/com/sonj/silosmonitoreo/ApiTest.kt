package com.sonj.silosmonitoreo

import com.sonj.silosmonitoreo.data.api.GranjaApiResponse
import com.sonj.silosmonitoreo.data.api.Resultado
import com.sonj.silosmonitoreo.data.api.UsuarioApiResponse
import com.sonj.silosmonitoreo.data.api.llamarApi
import com.sonj.silosmonitoreo.data.api.mensajeError
import com.sonj.silosmonitoreo.data.api.toModel
import com.sonj.silosmonitoreo.data.api.toUsuario
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.model.TipoMovimiento
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiTest {

    // ---------- Mappers ----------

    @Test
    fun siloApi_se_convierte_a_modelo() {
        val silo = siloDePrueba(actuales = 50.0).toModel()
        assertEquals("Silo A1", silo.nombre)
        assertEquals(50, silo.porcentaje)
        assertEquals("Trigo", silo.tipoGrano)
        assertEquals(100, silo.capacidadToneladas)
        assertEquals(50, silo.toneladasActuales)
    }

    @Test
    fun granja_sin_ubicacion_queda_con_texto_vacio() {
        val granja = GranjaApiResponse("g1", "Granja Uno", null, listOf(siloDePrueba())).toModel()
        assertEquals("", granja.ubicacion)
        assertEquals(1, granja.silos.size)
    }

    @Test
    fun movimiento_convierte_kilos_a_toneladas() {
        val mov = movimientoDePrueba(tipo = "SALIDA").toModel("Silo A1", "Granja Uno")
        assertEquals(10, mov.cantidadToneladas)
        assertEquals(TipoMovimiento.SALIDA, mov.tipo)
        assertEquals("admin@silos.com", mov.responsable)
        assertEquals("prueba", mov.notas)
    }

    @Test
    fun usuario_api_se_convierte_sin_guardar_la_contrasena() {
        val u = UsuarioApiResponse("1", "Ana", "ana@silos.com", "1990-05-20", "ADMINISTRADOR").toUsuario("tok")
        assertEquals(RolUsuario.ADMINISTRADOR, u.rol)
        assertEquals("", u.contrasena)
        assertEquals("tok", u.tokenSesion)
    }

    // ---------- Modelo ----------

    @Test
    fun silo_calcula_estado_e_icono() {
        assertEquals(EstadoSilo.CRITICO, Silo("a", "A", 10).estado)
        assertEquals(EstadoSilo.ADVERTENCIA, Silo("a", "A", 20).estado)
        assertEquals(EstadoSilo.NORMAL, Silo("a", "A", 80).estado)
        assertEquals("📦", Silo("a", "A", 80, tipoGrano = "Otro").iconoGrano)
    }

    @Test
    fun silo_calcula_toneladas_actuales_por_defecto() {
        assertEquals(60, Silo("a", "A", 40, capacidadToneladas = 150).toneladasActuales)
    }

    // ---------- llamarApi y mensajes de error ----------

    @Test
    fun llamarApi_respuesta_exitosa_devuelve_ok() = runTest {
        val r = llamarApi { Response.success("hola") }
        assertEquals(Resultado.Ok("hola"), r)
    }

    @Test
    fun llamarApi_sin_red_devuelve_error_de_conexion() = runTest {
        val r = llamarApi<String> { throw IOException("sin red") }
        assertTrue((r as Resultado.Error).mensaje.contains("conectar"))
    }

    @Test
    fun llamarApi_excepcion_inesperada_devuelve_error() = runTest {
        val r = llamarApi<String> { throw IllegalStateException("fallo") }
        assertTrue((r as Resultado.Error).mensaje.contains("fallo"))
    }

    @Test
    fun mensajeError_usa_el_mensaje_del_backend() {
        val resp = errorHttp<Unit>(400, """{"message":"Capacidad invalida"}""")
        assertEquals("Capacidad invalida", resp.mensajeError())
    }

    @Test
    fun mensajeError_sin_cuerpo_usa_el_codigo_http() {
        assertTrue(errorHttp<Unit>(401).mensajeError().contains("expirada"))
        assertTrue(errorHttp<Unit>(403).mensajeError().contains("permisos"))
        assertTrue(errorHttp<Unit>(500).mensajeError().contains("500"))
    }
}