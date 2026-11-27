package com.sonj.silosmonitoreo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.data.api.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SiloFormUiState(
    val siloId: String? = null,          // null = crear, con valor = editar
    val nombre: String = "",
    val producto: String = "",
    val capacidad: String = "",
    val toneladas: String = "",
    val granjaId: String = "",
    val errorNombre: String? = null,
    val errorProducto: String? = null,
    val errorCapacidad: String? = null,
    val errorToneladas: String? = null,
    val mensajeError: String? = null,
    val guardando: Boolean = false,
    val operacionExitosa: Boolean = false
)

class SiloFormViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SiloFormUiState())
    val uiState: StateFlow<SiloFormUiState> = _uiState.asStateFlow()

    val granjas get() = SilosRepository.granjas

    private var inicializado = false

    fun inicializar(siloId: String?) {
        if (inicializado) return
        inicializado = true
        if (siloId == null) {
            _uiState.update { it.copy(granjaId = granjas.firstOrNull()?.id ?: "") }
            return
        }
        val encontrado = SilosRepository.obtenerSilo(siloId)
        if (encontrado == null) {
            _uiState.update { it.copy(mensajeError = "No se encontro el silo.") }
            return
        }
        val (granja, silo) = encontrado
        _uiState.update {
            it.copy(
                siloId = siloId,
                nombre = silo.nombre,
                producto = silo.tipoGrano,
                capacidad = silo.capacidadToneladas.toString(),
                toneladas = silo.toneladasActuales.toString(),
                granjaId = granja.id
            )
        }
    }

    fun onNombreCambiado(valor: String) =
        _uiState.update { it.copy(nombre = valor, errorNombre = ValidadorSilo.errorNombre(valor), mensajeError = null) }

    fun onProductoCambiado(valor: String) =
        _uiState.update { it.copy(producto = valor, errorProducto = ValidadorSilo.errorProducto(valor), mensajeError = null) }

    fun onCapacidadCambiada(valor: String) = _uiState.update {
        it.copy(
            capacidad = valor,
            errorCapacidad = ValidadorSilo.errorCapacidad(valor),
            errorToneladas = if (it.toneladas.isBlank()) null else ValidadorSilo.errorToneladas(it.toneladas, valor),
            mensajeError = null
        )
    }

    fun onToneladasCambiadas(valor: String) = _uiState.update {
        it.copy(toneladas = valor, errorToneladas = ValidadorSilo.errorToneladas(valor, it.capacidad), mensajeError = null)
    }

    fun onGranjaSeleccionada(granjaId: String) =
        _uiState.update { it.copy(granjaId = granjaId, mensajeError = null) }

    fun guardar() {
        val e = _uiState.value
        val errNombre = ValidadorSilo.errorNombre(e.nombre)
        val errProducto = ValidadorSilo.errorProducto(e.producto)
        val errCapacidad = ValidadorSilo.errorCapacidad(e.capacidad)
        val errToneladas = ValidadorSilo.errorToneladas(e.toneladas, e.capacidad)

        if (errNombre != null || errProducto != null || errCapacidad != null || errToneladas != null) {
            _uiState.update {
                it.copy(
                    errorNombre = errNombre,
                    errorProducto = errProducto,
                    errorCapacidad = errCapacidad,
                    errorToneladas = errToneladas,
                    mensajeError = "Revisa los campos marcados en rojo."
                )
            }
            return
        }
        if (e.granjaId.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Selecciona una granja.") }
            return
        }

        val id = e.siloId
        val capacidad = e.capacidad.trim().toInt()
        val toneladas = e.toneladas.trim().toInt()
        ejecutar {
            if (id == null) {
                SilosRepository.crearSilo(e.nombre, e.producto, capacidad, toneladas, e.granjaId)
            } else {
                SilosRepository.actualizarSilo(id, e.nombre, e.producto, capacidad, toneladas, e.granjaId)
            }
        }
    }

    fun eliminar() {
        val id = _uiState.value.siloId ?: return
        ejecutar { SilosRepository.eliminarSilo(id) }
    }

    private fun ejecutar(operacion: suspend () -> Resultado<Unit>) {
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null) }
            when (val r = operacion()) {
                is Resultado.Ok -> _uiState.update { it.copy(guardando = false, operacionExitosa = true) }
                is Resultado.Error -> _uiState.update { it.copy(guardando = false, mensajeError = r.mensaje) }
            }
        }
    }
}