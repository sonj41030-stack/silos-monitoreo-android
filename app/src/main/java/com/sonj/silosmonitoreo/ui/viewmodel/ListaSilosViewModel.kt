package com.sonj.silosmonitoreo.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Estado de la UI para la pantalla principal de lista de silos
data class ListaSilosUiState(
    val filtroGranja: String = "Todas",
    val filtroEstado: EstadoSilo? = null,
    val rol: RolUsuario = RolUsuario.OPERADOR
) {
    // Granjas filtradas calculadas desde el estado
    fun granjasFiltradas(granjas: List<Granja>): List<Granja> {
        return granjas.mapNotNull { granja ->
            if (filtroGranja != "Todas" && granja.nombre != filtroGranja) {
                null
            } else {
                val silosCoincidentes = granja.silos.filter { silo ->
                    filtroEstado == null || silo.estado == filtroEstado
                }
                if (silosCoincidentes.isNotEmpty()) granja.copy(silos = silosCoincidentes) else null
            }
        }
    }

    // Métricas globales para Jefatura
    fun promedioOcupacion(granjas: List<Granja>): Int {
        val todos = granjas.flatMap { it.silos }
        val totalCap = todos.sumOf { it.capacidadToneladas }
        val totalLleno = todos.sumOf { (it.porcentaje * it.capacidadToneladas) / 100 }
        return if (totalCap > 0) (totalLleno * 100) / totalCap else 0
    }

    fun totalStock(granjas: List<Granja>): Int =
        granjas.flatMap { it.silos }.sumOf { (it.porcentaje * it.capacidadToneladas) / 100 }

    fun totalCapacidad(granjas: List<Granja>): Int =
        granjas.flatMap { it.silos }.sumOf { it.capacidadToneladas }

    fun totalCriticos(granjas: List<Granja>): Int =
        granjas.flatMap { it.silos }.count { it.estado == EstadoSilo.CRITICO }

    fun totalSilos(granjas: List<Granja>): Int =
        granjas.flatMap { it.silos }.size
}

class ListaSilosViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ListaSilosUiState())
    val uiState: StateFlow<ListaSilosUiState> = _uiState.asStateFlow()

    // Expone las granjas directamente desde el Repository (reactivas con mutableStateListOf)
    val granjas get() = SilosRepository.granjas
    val alertas get() = SilosRepository.alertas

    fun inicializarRol(rol: RolUsuario) {
        _uiState.update { it.copy(rol = rol) }
    }

    fun onFiltroGranjaChanged(nombreGranja: String) {
        _uiState.update { it.copy(filtroGranja = nombreGranja) }
    }

    fun onFiltroEstadoChanged(estado: EstadoSilo?) {
        _uiState.update { it.copy(filtroEstado = estado) }
    }

    fun limpiarFiltros() {
        _uiState.update { it.copy(filtroGranja = "Todas", filtroEstado = null) }
    }
}
