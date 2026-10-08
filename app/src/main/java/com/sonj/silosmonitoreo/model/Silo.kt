package com.sonj.silosmonitoreo.model

data class Silo(
    val id: String,
    val nombre: String,
    val porcentaje: Int,
    val tipoGrano: String = "Maíz",
    val capacidadToneladas: Int = 100
) {
    val estado: EstadoSilo
        get() = EstadoSilo.desdePorcentaje(porcentaje.toFloat())
}
