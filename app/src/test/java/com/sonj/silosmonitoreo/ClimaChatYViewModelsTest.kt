package com.sonj.silosmonitoreo

import android.app.Application
import android.graphics.Bitmap
import com.sonj.silosmonitoreo.data.ChatRepository
import com.sonj.silosmonitoreo.data.Clima
import com.sonj.silosmonitoreo.data.ClimaRepository
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.data.api.ClimaActual
import com.sonj.silosmonitoreo.data.api.ClimaApi
import com.sonj.silosmonitoreo.data.api.ClimaResponse
import com.sonj.silosmonitoreo.data.api.Resultado
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.model.TipoMovimiento
import com.sonj.silosmonitoreo.ui.viewmodel.FormularioMovimientoViewModel
import com.sonj.silosmonitoreo.ui.viewmodel.ListaSilosViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ClimaChatYViewModelsTest {

    // ---------- ClimaRepository (API externa) ----------

    @Test
    fun clima_ok_devuelve_temperatura_y_humedad() = runTest {
        val api = mockk<ClimaApi>()
        coEvery { api.clima(any(), any(), any()) } returns Response.success(ClimaResponse(ClimaActual(21.5, 60)))
        ClimaRepository.api = api

        val r = ClimaRepository.obtener()

        assertEquals(Resultado.Ok(Clima(21.5, 60)), r)
    }

    @Test
    fun clima_con_error_devuelve_error() = runTest {
        val api = mockk<ClimaApi>()
        coEvery { api.clima(any(), any(), any()) } throws IOException("sin red")
        ClimaRepository.api = api

        assertTrue(ClimaRepository.obtener() is Resultado.Error)
    }

    // ---------- ChatRepository ----------

    @Test
    fun chat_ignora_mensajes_vacios_y_guarda_los_validos() {
        val antes = ChatRepository.mensajes.size
        ChatRepository.enviarMensaje("Ana", RolUsuario.ADMINISTRADOR, "   ")
        assertEquals(antes, ChatRepository.mensajes.size)

        ChatRepository.enviarMensaje("Ana", RolUsuario.ADMINISTRADOR, "  Hola  ", esUrgente = true)
        assertEquals(antes + 1, ChatRepository.mensajes.size)
        assertEquals("Hola", ChatRepository.mensajes.last().mensaje)
        assertTrue(ChatRepository.mensajes.last().esUrgente)
    }

    // ---------- ListaSilosViewModel ----------

    @Test
    fun lista_viewmodel_actualiza_rol_y_filtros() {
        SilosRepository.limpiar()
        SilosRepository.granjas.add(Granja("g1", "Granja Uno", "Melipilla", listOf(Silo("s1", "A1", 10))))
        val vm = ListaSilosViewModel()

        vm.inicializarRol(RolUsuario.JEFATURA)
        vm.onFiltroGranjaChanged("Granja Uno")
        vm.onFiltroEstadoChanged(EstadoSilo.CRITICO)

        assertEquals(RolUsuario.JEFATURA, vm.uiState.value.rol)
        assertEquals("Granja Uno", vm.uiState.value.filtroGranja)
        assertEquals(EstadoSilo.CRITICO, vm.uiState.value.filtroEstado)
        assertEquals(1, vm.granjas.size)

        vm.limpiarFiltros()
        assertEquals("Todas", vm.uiState.value.filtroGranja)
        assertNull(vm.uiState.value.filtroEstado)
        SilosRepository.limpiar()
    }

    // ---------- FormularioMovimientoViewModel (foto y permisos) ----------

    @Test
    fun formulario_maneja_foto_y_permiso_de_camara() {
        val vm = FormularioMovimientoViewModel(mockk<Application>(relaxed = true))

        vm.onPermisoDenegado()
        assertNotNull(vm.uiState.value.mensajePermiso)

        vm.onPermisoConcedido()
        assertNull(vm.uiState.value.mensajePermiso)

        vm.onFotoCapturada(mockk<Bitmap>())
        assertNotNull(vm.uiState.value.fotoEvidencia)

        vm.onSiloSeleccionado("s9")
        vm.onTipoCambiado(TipoMovimiento.SALIDA)
        vm.onNotasCambiadas("nota")
        assertEquals("s9", vm.uiState.value.siloSeleccionadoId)
        assertEquals(TipoMovimiento.SALIDA, vm.uiState.value.tipoMovimiento)
        assertEquals("nota", vm.uiState.value.notas)
    }
}