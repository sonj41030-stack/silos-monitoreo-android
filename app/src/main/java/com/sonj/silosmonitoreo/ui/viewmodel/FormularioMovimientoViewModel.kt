package com.sonj.silosmonitoreo.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.model.TipoMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FormularioMovimientoUiState(
    val siloSeleccionadoId: String = "",
    val tipoMovimiento: TipoMovimiento = TipoMovimiento.ENTRADA,
    val cantidad: String = "",
    val responsable: String = "",
    val notas: String = "",
    val fotoEvidencia: Bitmap? = null,
    val mensajePermiso: String? = null,
    val mensajeError: String? = null,
    val movimientoGuardado: Boolean = false
)

class FormularioMovimientoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioMovimientoUiState())
    val uiState: StateFlow<FormularioMovimientoUiState> = _uiState.asStateFlow()

    fun obtenerSilos(): List<Pair<String, Silo>> {
        return SilosRepository.granjas.flatMap { granja ->
            granja.silos.map { silo -> Pair(granja.nombre, silo) }
        }
    }

    fun inicializarSilo(siloIdInicial: String?) {
        if (_uiState.value.siloSeleccionadoId.isNotBlank()) return
        val id = siloIdInicial ?: obtenerSilos().firstOrNull()?.second?.id ?: ""
        _uiState.update { it.copy(siloSeleccionadoId = id) }
    }

    fun onSiloSeleccionado(siloId: String) {
        _uiState.update { it.copy(siloSeleccionadoId = siloId) }
    }

    fun onTipoCambiado(tipo: TipoMovimiento) {
        _uiState.update { it.copy(tipoMovimiento = tipo) }
    }

    fun onCantidadCambiada(valor: String) {
        _uiState.update { it.copy(cantidad = valor, mensajeError = null) }
    }

    fun onResponsableCambiado(valor: String) {
        _uiState.update { it.copy(responsable = valor, mensajeError = null) }
    }

    fun onNotasCambiadas(valor: String) {
        _uiState.update { it.copy(notas = valor) }
    }

    fun onFotoCapturada(bitmap: Bitmap) {
        _uiState.update { it.copy(fotoEvidencia = bitmap, mensajePermiso = null) }
    }

    fun onPermisoDenegado() {
        _uiState.update {
            it.copy(mensajePermiso = "Permiso de cámara denegado. Actívalo en Ajustes para adjuntar fotos.")
        }
    }

    fun onPermisoConcedido() {
        _uiState.update { it.copy(mensajePermiso = null) }
    }

    fun guardarMovimiento() {
        val estado = _uiState.value
        val cantidad = estado.cantidad.toIntOrNull()

        when {
            estado.siloSeleccionadoId.isBlank() -> {
                _uiState.update { it.copy(mensajeError = "Por favor selecciona un silo.") }
            }
            cantidad == null || cantidad <= 0 -> {
                _uiState.update {
                    it.copy(mensajeError = "Por favor ingresa una cantidad válida en toneladas.")
                }
            }
            estado.responsable.isBlank() -> {
                _uiState.update {
                    it.copy(mensajeError = "Por favor ingresa el nombre del responsable.")
                }
            }
            else -> {
                val exito = SilosRepository.registrarMovimiento(
                    siloId = estado.siloSeleccionadoId,
                    tipo = estado.tipoMovimiento,
                    cantidadTn = cantidad,
                    responsable = estado.responsable.trim(),
                    notas = estado.notas.trim()
                )
                if (exito) {
                    _uiState.update { it.copy(movimientoGuardado = true, mensajeError = null) }
                } else {
                    _uiState.update { it.copy(mensajeError = "Error al registrar el movimiento.") }
                }
            }
        }
    }
}