package com.sonj.silosmonitoreo.model

enum class  EstadoSilo(val etiqueta: String){
    NORMAL("Normal"),
    ADVERTENCIA("Advertencia"),
    CRITICO("Critico");

    companion object{
        fun desdePorcentaje(porcentaje: Float): EstadoSilo = when {
            porcentaje < 15f -> EstadoSilo.CRITICO
            porcentaje < 30f -> EstadoSilo.ADVERTENCIA
            else -> EstadoSilo.NORMAL
        }
    }
}
