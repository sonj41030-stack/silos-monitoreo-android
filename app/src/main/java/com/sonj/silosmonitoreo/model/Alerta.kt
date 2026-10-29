package com.sonj.silosmonitoreo.model

import java.time.LocalDateTime

data class Alerta(
    val id: String,
    val siloId: String,
    val siloNombre: String,
    val granjaNombre: String,
    val estado: EstadoSilo,
    val porcentajeActual: Int,
    val mensaje: String,
    val fechaHora: LocalDateTime = LocalDateTime.now(),
    var leida: Boolean = false
)
