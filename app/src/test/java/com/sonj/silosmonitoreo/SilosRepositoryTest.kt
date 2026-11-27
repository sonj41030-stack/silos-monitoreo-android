package com.sonj.silosmonitoreo

import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.data.api.AjusteSiloRequest
import com.sonj.silosmonitoreo.data.api.MovimientoApiRequest
import com.sonj.silosmonitoreo.data.api.MovimientosApi
import com.sonj.silosmonitoreo.data.api.Resultado
import com.sonj.silosmonitoreo.data.api.SiloApiRequest
import com.sonj.silosmonitoreo.data.api.SilosApi
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.TipoMovimiento
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class SilosRepositoryTest {

    private val silosApi = mockk<SilosApi>()
    private val movimientosApi = mockk<MovimientosApi>()

    @Before
    fun setUp() {
        SilosRepository.silosApi = silosApi
        SilosRepository.movimientosApi = movimientosApi
        SilosRepository.limpiar()
        coEvery { silosApi.granjas() } returns Response.success(listOf(granjaDePrueba(siloDePrueba())))
        coEvery { movimientosApi.listar(any()) } returns Response.success(listOf(movimientoDePrueba()))
    }

    // ---------- Lectura ----------

    @Test
    fun cargarGranjas_llena_la_lista() = runTest {
        val r = SilosRepository.cargarGranjas()
        assertTrue(r is Resultado.Ok)
        assertEquals(1, SilosRepository.granjas.size)
        assertEquals("Silo A1", SilosRepository.granjas[0].silos[0].nombre)
    }

    @Test
    fun cargarGranjas_sin_conexion_devuelve_error() = runTest {
        coEvery { silosApi.granjas() } throws IOException("sin red")
        val r = SilosRepository.cargarGranjas()
        assertTrue(r is Resultado.Error)
        assertTrue((r as Resultado.Error).mensaje.contains("conectar"))
    }

    @Test
    fun cargarGranjas_genera_alerta_critica_si_el_nivel_es_bajo() = runTest {
        coEvery { silosApi.granjas() } returns Response.success(listOf(granjaDePrueba(siloDePrueba(actuales = 10.0))))
        SilosRepository.cargarGranjas()
        assertEquals(1, SilosRepository.alertas.size)
        assertEquals(EstadoSilo.CRITICO, SilosRepository.alertas[0].estado)
    }

    @Test
    fun cargarGranjas_genera_alerta_de_advertencia() = runTest {
        coEvery { silosApi.granjas() } returns Response.success(listOf(granjaDePrueba(siloDePrueba(actuales = 25.0))))
        SilosRepository.cargarGranjas()
        assertEquals(EstadoSilo.ADVERTENCIA, SilosRepository.alertas[0].estado)
    }

    @Test
    fun cargarMovimientos_agrega_nombres_de_silo_y_granja() = runTest {
        SilosRepository.cargarGranjas()
        val r = SilosRepository.cargarMovimientos()
        assertTrue(r is Resultado.Ok)
        val mov = SilosRepository.movimientos[0]
        assertEquals("Silo A1", mov.siloNombre)
        assertEquals("Granja Uno", mov.granjaNombre)
        assertEquals(10, mov.cantidadToneladas)
        assertEquals(TipoMovimiento.ENTRADA, mov.tipo)
    }

    @Test
    fun cargarMovimientos_sin_granjas_marca_silo_desconocido() = runTest {
        SilosRepository.cargarMovimientos()
        assertEquals("Silo desconocido", SilosRepository.movimientos[0].siloNombre)
    }

    @Test
    fun obtenerSilo_encuentra_y_no_encuentra() = runTest {
        SilosRepository.cargarGranjas()
        assertNotNull(SilosRepository.obtenerSilo("s1"))
        assertNull(SilosRepository.obtenerSilo("no-existe"))
    }

    @Test
    fun limpiar_vacia_todas_las_listas() = runTest {
        SilosRepository.cargarGranjas()
        SilosRepository.cargarMovimientos()
        SilosRepository.limpiar()
        assertTrue(SilosRepository.granjas.isEmpty())
        assertTrue(SilosRepository.movimientos.isEmpty())
        assertTrue(SilosRepository.alertas.isEmpty())
    }

    // ---------- Movimientos (dos microservicios) ----------

    @Test
    fun registrarMovimiento_ajusta_el_silo_y_guarda_el_historial() = runTest {
        coEvery { silosApi.ajustar("s1", any()) } returns Response.success(siloDePrueba())
        coEvery { movimientosApi.registrar(any()) } returns Response.success(movimientoDePrueba())

        val r = SilosRepository.registrarMovimiento("s1", TipoMovimiento.ENTRADA, 10, " prueba ")

        assertTrue(r is Resultado.Ok)
        coVerify { silosApi.ajustar("s1", AjusteSiloRequest("ENTRADA", 10000.0)) }
        coVerify { movimientosApi.registrar(MovimientoApiRequest("s1", "ENTRADA", 10000.0, "prueba")) }
        assertEquals(1, SilosRepository.movimientos.size)
    }

    @Test
    fun registrarMovimiento_con_notas_vacias_envia_null() = runTest {
        coEvery { silosApi.ajustar("s1", any()) } returns Response.success(siloDePrueba())
        coEvery { movimientosApi.registrar(any()) } returns Response.success(movimientoDePrueba())

        SilosRepository.registrarMovimiento("s1", TipoMovimiento.SALIDA, 5, "   ")

        coVerify { movimientosApi.registrar(MovimientoApiRequest("s1", "SALIDA", 5000.0, null)) }
    }

    @Test
    fun registrarMovimiento_si_el_ajuste_falla_no_guarda_historial() = runTest {
        coEvery { silosApi.ajustar("s1", any()) } returns
            errorHttp(400, """{"message":"No hay suficiente producto en el silo para esa salida."}""")

        val r = SilosRepository.registrarMovimiento("s1", TipoMovimiento.SALIDA, 500, "")

        assertTrue(r is Resultado.Error)
        assertEquals("No hay suficiente producto en el silo para esa salida.", (r as Resultado.Error).mensaje)
        coVerify(exactly = 0) { movimientosApi.registrar(any()) }
    }

    @Test
    fun registrarMovimiento_si_el_historial_falla_revierte_el_ajuste() = runTest {
        coEvery { silosApi.ajustar("s1", any()) } returns Response.success(siloDePrueba())
        coEvery { movimientosApi.registrar(any()) } returns errorHttp(500)

        val r = SilosRepository.registrarMovimiento("s1", TipoMovimiento.ENTRADA, 10, "")

        assertTrue(r is Resultado.Error)
        assertTrue((r as Resultado.Error).mensaje.contains("stock"))
        // compensacion: se manda el ajuste inverso
        coVerify { silosApi.ajustar("s1", AjusteSiloRequest("SALIDA", 10000.0)) }
    }

    // ---------- CRUD de silos ----------

    @Test
    fun crearSilo_envia_los_datos_y_recarga_la_lista() = runTest {
        coEvery { silosApi.crearSilo(any()) } returns Response.success(siloDePrueba())

        val r = SilosRepository.crearSilo(" Silo Nuevo ", "Trigo", 100, 50, "g1")

        assertTrue(r is Resultado.Ok)
        coVerify { silosApi.crearSilo(SiloApiRequest("Silo Nuevo", "Trigo", 100.0, 50.0, "g1")) }
        coVerify { silosApi.granjas() }
    }

    @Test
    fun crearSilo_sin_permisos_devuelve_error_y_no_recarga() = runTest {
        coEvery { silosApi.crearSilo(any()) } returns errorHttp(403)

        val r = SilosRepository.crearSilo("Silo Nuevo", "Trigo", 100, 50, "g1")

        assertTrue(r is Resultado.Error)
        coVerify(exactly = 0) { silosApi.granjas() }
    }

    @Test
    fun actualizarSilo_envia_el_id_y_los_datos() = runTest {
        coEvery { silosApi.actualizarSilo("s1", any()) } returns Response.success(siloDePrueba())

        val r = SilosRepository.actualizarSilo("s1", "Silo Editado", "Maiz", 200, 80, "g1")

        assertTrue(r is Resultado.Ok)
        coVerify { silosApi.actualizarSilo("s1", SiloApiRequest("Silo Editado", "Maiz", 200.0, 80.0, "g1")) }
    }

    @Test
    fun eliminarSilo_llama_al_endpoint_y_recarga() = runTest {
        coEvery { silosApi.eliminarSilo("s1") } returns Response.success(Unit)

        val r = SilosRepository.eliminarSilo("s1")

        assertTrue(r is Resultado.Ok)
        coVerify { silosApi.eliminarSilo("s1") }
        coVerify { silosApi.granjas() }
    }
}