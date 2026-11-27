package com.sonj.silosmonitoreo

import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.data.api.SiloApiRequest
import com.sonj.silosmonitoreo.data.api.SilosApi
import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.ui.viewmodel.SiloFormViewModel
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
class SiloFormViewModelTest {

    private val silosApi = mockk<SilosApi>()
    private lateinit var vm: SiloFormViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        SilosRepository.silosApi = silosApi
        SilosRepository.limpiar()
        SilosRepository.granjas.add(
            Granja("g1", "Granja Uno", "Melipilla", listOf(Silo("s1", "Silo A1", 50, "Trigo", 100)))
        )
        coEvery { silosApi.granjas() } returns Response.success(listOf(granjaDePrueba(siloDePrueba())))
        vm = SiloFormViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun llenarFormularioValido() {
        vm.onNombreCambiado("Silo Nuevo")
        vm.onProductoCambiado("Trigo")
        vm.onCapacidadCambiada("100")
        vm.onToneladasCambiadas("50")
    }

    @Test
    fun inicializar_en_modo_crear_selecciona_la_primera_granja() {
        vm.inicializar(null)
        assertEquals("g1", vm.uiState.value.granjaId)
        assertEquals(null, vm.uiState.value.siloId)
    }

    @Test
    fun inicializar_en_modo_editar_precarga_los_datos() {
        vm.inicializar("s1")
        val e = vm.uiState.value
        assertEquals("s1", e.siloId)
        assertEquals("Silo A1", e.nombre)
        assertEquals("Trigo", e.producto)
        assertEquals("100", e.capacidad)
        assertEquals("50", e.toneladas)
        assertEquals("g1", e.granjaId)
    }

    @Test
    fun inicializar_con_silo_inexistente_muestra_error() {
        vm.inicializar("no-existe")
        assertNotNull(vm.uiState.value.mensajeError)
    }

    @Test
    fun guardar_con_campos_vacios_muestra_errores_y_no_llama_a_la_api() {
        vm.inicializar(null)
        vm.guardar()
        val e = vm.uiState.value
        assertNotNull(e.errorNombre)
        assertNotNull(e.errorProducto)
        assertNotNull(e.errorCapacidad)
        assertNotNull(e.errorToneladas)
        assertFalse(e.operacionExitosa)
        coVerify(exactly = 0) { silosApi.crearSilo(any()) }
    }

    @Test
    fun toneladas_mayores_a_la_capacidad_marcan_error() {
        vm.onCapacidadCambiada("100")
        vm.onToneladasCambiadas("150")
        assertNotNull(vm.uiState.value.errorToneladas)
    }

    @Test
    fun cambiar_la_capacidad_revalida_las_toneladas() {
        vm.onCapacidadCambiada("100")
        vm.onToneladasCambiadas("80")
        vm.onCapacidadCambiada("50")
        assertNotNull(vm.uiState.value.errorToneladas)
    }

    @Test
    fun guardar_valido_crea_el_silo() {
        coEvery { silosApi.crearSilo(any()) } returns Response.success(siloDePrueba())
        vm.inicializar(null)
        llenarFormularioValido()

        vm.guardar()

        assertTrue(vm.uiState.value.operacionExitosa)
        coVerify { silosApi.crearSilo(SiloApiRequest("Silo Nuevo", "Trigo", 100.0, 50.0, "g1")) }
    }

    @Test
    fun guardar_en_modo_editar_actualiza_el_silo() {
        coEvery { silosApi.actualizarSilo("s1", any()) } returns Response.success(siloDePrueba())
        vm.inicializar("s1")
        vm.onNombreCambiado("Silo Editado")

        vm.guardar()

        assertTrue(vm.uiState.value.operacionExitosa)
        coVerify { silosApi.actualizarSilo("s1", SiloApiRequest("Silo Editado", "Trigo", 100.0, 50.0, "g1")) }
    }

    @Test
    fun eliminar_borra_el_silo() {
        coEvery { silosApi.eliminarSilo("s1") } returns Response.success(Unit)
        vm.inicializar("s1")

        vm.eliminar()

        assertTrue(vm.uiState.value.operacionExitosa)
        coVerify { silosApi.eliminarSilo("s1") }
    }

    @Test
    fun si_la_api_falla_muestra_el_error_y_no_termina() {
        coEvery { silosApi.crearSilo(any()) } returns errorHttp(403)
        vm.inicializar(null)
        llenarFormularioValido()

        vm.guardar()

        assertNotNull(vm.uiState.value.mensajeError)
        assertFalse(vm.uiState.value.operacionExitosa)
        assertFalse(vm.uiState.value.guardando)
    }
}