package com.sonj.silosmonitoreo

import android.app.Application
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.data.api.MovimientosApi
import com.sonj.silosmonitoreo.data.api.SilosApi
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.model.TipoMovimiento
import com.sonj.silosmonitoreo.ui.viewmodel.FormularioMovimientoViewModel
import com.sonj.silosmonitoreo.ui.viewmodel.ListaSilosUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MovimientoYListaViewModelTest {

    private val silosApi = mockk<SilosApi>()
    private val movimientosApi = mockk<MovimientosApi>()

    private val granjas = listOf(
        Granja("g1", "Granja Uno", "Melipilla", listOf(Silo("s1", "Silo A1", 10, "Trigo", 100), Silo("s2", "Silo A2", 80, "Maiz", 200))),
        Granja("g2", "Granja Dos", "Puangue", listOf(Silo("s3", "Silo B1", 25, "Soya", 100)))
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        SilosRepository.silosApi = silosApi
        SilosRepository.movimientosApi = movimientosApi
        SilosRepository.limpiar()
        SilosRepository.granjas.addAll(granjas)
        coEvery { silosApi.granjas() } returns Response.success(listOf(granjaDePrueba(siloDePrueba())))
        coEvery { movimientosApi.listar(any()) } returns Response.success(listOf(movimientoDePrueba()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- FormularioMovimientoViewModel ----------

    @Test
    fun formulario_inicializa_con_el_silo_indicado() {
        val vm = FormularioMovimientoViewModel(mockk<Application>(relaxed = true))
        vm.inicializarSilo("s2")
        assertEquals("s2", vm.uiState.value.siloSeleccionadoId)
        assertEquals(3, vm.obtenerSilos().size)
    }

    @Test
    fun formulario_invalido_no_llama_a_la_api() {
        val vm = FormularioMovimientoViewModel(mockk<Application>(relaxed = true))
        vm.inicializarSilo("s1")
        vm.guardarMovimiento()
        assertNotNull(vm.uiState.value.errorCantidad)
        assertNotNull(vm.uiState.value.errorResponsable)
        assertFalse(vm.uiState.value.movimientoGuardado)
        coVerify(exactly = 0) { silosApi.ajustar(any(), any()) }
    }

    @Test
    fun formulario_valido_registra_el_movimiento() {
        coEvery { silosApi.ajustar("s1", any()) } returns Response.success(siloDePrueba())
        coEvery { movimientosApi.registrar(any()) } returns Response.success(movimientoDePrueba())
        val vm = FormularioMovimientoViewModel(mockk<Application>(relaxed = true))
        vm.inicializarSilo("s1")
        vm.onTipoCambiado(TipoMovimiento.ENTRADA)
        vm.onCantidadCambiada("10")
        vm.onResponsableCambiado("Carlos")

        vm.guardarMovimiento()

        assertTrue(vm.uiState.value.movimientoGuardado)
    }

    @Test
    fun formulario_muestra_el_error_del_backend() {
        coEvery { silosApi.ajustar("s1", any()) } returns errorHttp(400, """{"message":"La entrada supera la capacidad del silo."}""")
        val vm = FormularioMovimientoViewModel(mockk<Application>(relaxed = true))
        vm.inicializarSilo("s1")
        vm.onCantidadCambiada("10")
        vm.onResponsableCambiado("Carlos")

        vm.guardarMovimiento()

        assertEquals("La entrada supera la capacidad del silo.", vm.uiState.value.mensajeError)
        assertFalse(vm.uiState.value.movimientoGuardado)
    }

    // ---------- ListaSilosUiState ----------

    @Test
    fun lista_filtra_por_granja() {
        val r = ListaSilosUiState(filtroGranja = "Granja Dos").granjasFiltradas(granjas)
        assertEquals(1, r.size)
        assertEquals("Granja Dos", r[0].nombre)
    }

    @Test
    fun lista_filtra_por_estado() {
        val r = ListaSilosUiState(filtroEstado = EstadoSilo.CRITICO).granjasFiltradas(granjas)
        assertEquals(1, r.size)
        assertEquals(1, r[0].silos.size)
    }

    @Test
    fun lista_sin_filtros_devuelve_todo() {
        assertEquals(2, ListaSilosUiState().granjasFiltradas(granjas).size)
    }

    @Test
    fun lista_calcula_metricas_de_jefatura() {
        val e = ListaSilosUiState()
        assertEquals(3, e.totalSilos(granjas))
        assertEquals(400, e.totalCapacidad(granjas))
        assertEquals(195, e.totalStock(granjas)) // 10 + 160 + 25
        assertEquals(48, e.promedioOcupacion(granjas))
        assertEquals(1, e.totalCriticos(granjas))
    }
}