package com.sonj.silosmonitoreo.model

import java.time.LocalDateTime

enum class TamanoFuente(val etiqueta: String, val factorEscala: Float) {
    PEQUENA("Pequeña", 0.90f),
    NORMAL("Normal", 1.0f),
    GRANDE("Grande", 1.15f)
}

enum class EstiloTipografia(val etiqueta: String) {
    MODERNA("Moderna (Sans)"),
    SERIF("Clásica (Serif)"),
    INDUSTRIAL("Industrial (Mono)")
}

data class MensajeChat(
    val id: String,
    val remitenteNombre: String,
    val remitenteRol: RolUsuario,
    val mensaje: String,
    val fechaHora: LocalDateTime,
    val esUrgente: Boolean = false
)
