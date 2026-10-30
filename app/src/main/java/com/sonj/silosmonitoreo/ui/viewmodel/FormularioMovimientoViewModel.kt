package com.sonj.silosmonitoreo.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
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
    // Errores por campo (null = campo válido)
    val errorCantidad: String? = null,
    val errorResponsable: String? = null,
    val errorNotas: String? = null,
    val fotoEvidencia: Bitmap? = null,
    val mensajePermiso: String? = null,
    val mensajeError: String? = null,
    val movimientoGuardado: Boolean = false
)

// Cambia de ViewModel a AndroidViewModel para tener acceso al Context (necesario para SQLite)
class FormularioMovimientoViewModel(application: Application) : AndroidViewModel(application) {

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
        _uiState.update { it.copy(cantidad = valor, errorCantidad = ValidadorMovimiento.errorCantidad(valor), mensajeError = null) }
    }

    fun onResponsableCambiado(valor: String) {
        _uiState.update { it.copy(responsable = valor, errorResponsable = ValidadorMovimiento.errorResponsable(valor), mensajeError = null) }
    }

    fun onNotasCambiadas(valor: String) {
        _uiState.update { it.copy(notas = valor, errorNotas = ValidadorMovimiento.errorNotas(valor), mensajeError = null) }
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

        if (estado.siloSeleccionadoId.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Por favor selecciona un silo.") }
            return
        }

        // Validación por campo: cada regla vive en ValidadorMovimiento
        val errorCantidad = ValidadorMovimiento.errorCantidad(estado.cantidad)
        val errorResponsable = ValidadorMovimiento.errorResponsable(estado.responsable)
        val errorNotas = ValidadorMovimiento.errorNotas(estado.notas)

        if (errorCantidad != null || errorResponsable != null || errorNotas != null) {
            _uiState.update {
                it.copy(
                    errorCantidad = errorCantidad,
                    errorResponsable = errorResponsable,
                    errorNotas = errorNotas,
                    mensajeError = "Revisa los campos marcados en rojo."
                )
            }
            return
        }

        val cantidad = estado.cantidad.trim().toInt()

        // Todos los campos son válidos: se guarda en SQLite
        val context = getApplication<Application>().applicationContext
        val exito = SilosRepository.registrarMovimiento(
            context = context,
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
