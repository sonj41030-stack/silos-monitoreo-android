package com.sonj.silosmonitoreo

import com.sonj.silosmonitoreo.data.Clima
import com.sonj.silosmonitoreo.data.ClimaRepository
import com.sonj.silosmonitoreo.data.api.ClimaActual
import com.sonj.silosmonitoreo.data.api.ClimaApi
import com.sonj.silosmonitoreo.data.api.ClimaResponse
import com.sonj.silosmonitoreo.data.api.Resultado
import com.sonj.silosmonitoreo.ui.viewmodel.ListaSilosViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ClimaListaViewModelTest {

    private val api = mockk<ClimaApi>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        ClimaRepository.api = api
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun clima_empieza_en_null_y_se_carga_al_pedirlo() {
        coEvery { api.clima(any(), any(), any()) } returns Response.success(ClimaResponse(ClimaActual(18.0, 55)))
        val vm = ListaSilosViewModel()
        assertNull(vm.clima.value)

        vm.cargarClima()

        assertEquals(Resultado.Ok(Clima(18.0, 55)), vm.clima.value)
    }

    @Test
    fun clima_con_error_queda_como_error() {
        coEvery { api.clima(any(), any(), any()) } throws IOException("sin red")
        val vm = ListaSilosViewModel()

        vm.cargarClima()

        assertTrue(vm.clima.value is Resultado.Error)
    }
}