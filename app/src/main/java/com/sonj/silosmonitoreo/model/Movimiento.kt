package com.sonj.silosmonitoreo.model

import java.time.LocalDateTime

data class Movimiento(
    val id: String,
    val siloId: String,
    val siloNombre: String,
    val granjaNombre: String,
    val tipo: TipoMovimiento,
    val cantidadToneladas: Int,
    val fechaHora: LocalDateTime = LocalDateTime.now(),
    val responsable: String,
    val notas: String = ""
)
