package com.sonj.silosmonitoreo.data

import androidx.compose.runtime.mutableStateListOf
import com.sonj.silosmonitoreo.model.Alerta
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.Movimiento
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.model.TipoMovimiento
import java.time.LocalDateTime
import java.util.UUID

object SilosRepository {

    val granjas = mutableStateListOf<Granja>().apply {
        addAll(DatosPrueba.granjasDePrueba)
    }

    val movimientos = mutableStateListOf<Movimiento>().apply {
        add(
            Movimiento(
                id = "m1",
                siloId = "s1",
                siloNombre = "Silo 1",
                granjaNombre = "Granja Norte",
                tipo = TipoMovimiento.ENTRADA,
                cantidadToneladas = 30,
                fechaHora = LocalDateTime.now().minusHours(4),
                responsable = "Carlos Pérez",
                notas = "Carga rutinaria de maíz"
            )
        )
        add(
            Movimiento(
                id = "m2",
                siloId = "s3",
                siloNombre = "Silo 3",
                granjaNombre = "Granja Norte",
                tipo = TipoMovimiento.SALIDA,
                cantidadToneladas = 45,
                fechaHora = LocalDateTime.now().minusDays(1),
                responsable = "Ana Gómez",
                notas = "Despacho a planta de procesamiento"
            )
        )
        add(
            Movimiento(
                id = "m3",
                siloId = "s5",
                siloNombre = "Silo 2",
                granjaNombre = "Granja Sur",
                tipo = TipoMovimiento.SALIDA,
                cantidadToneladas = 50,
                fechaHora = LocalDateTime.now().minusHours(12),
                responsable = "Juan Rodríguez",
                notas = "Retiro por venta directa"
            )
        )
    }

    val alertas = mutableStateListOf<Alerta>()

    init {
        recalcularAlertas()
    }

    fun obtenerSilo(siloId: String): Pair<Granja, Silo>? {
        for (g in granjas) {
            val s = g.silos.firstOrNull { it.id == siloId }
            if (s != null) return Pair(g, s)
        }
        return null
    }

    fun registrarMovimiento(
        siloId: String,
        tipo: TipoMovimiento,
        cantidadTn: Int,
        responsable: String,
        notas: String
    ): Boolean {
        var realizado = false
        for (i in granjas.indices) {
            val granja = granjas[i]
            val indexSilo = granja.silos.indexOfFirst { it.id == siloId }
            if (indexSilo != -1) {
                val siloActual = granja.silos[indexSilo]
                val toneladasActuales = (siloActual.porcentaje * siloActual.capacidadToneladas) / 100
                val nuevasToneladas = if (tipo == TipoMovimiento.ENTRADA) {
                    (toneladasActuales + cantidadTn).coerceAtMost(siloActual.capacidadToneladas)
                } else {
                    (toneladasActuales - cantidadTn).coerceAtLeast(0)
                }
                val nuevoPorcentaje = ((nuevasToneladas.toFloat() / siloActual.capacidadToneladas) * 100).toInt()

                val siloActualizado = siloActual.copy(porcentaje = nuevoPorcentaje)
                val nuevosSilos = granja.silos.toMutableList().apply {
                    set(indexSilo, siloActualizado)
                }
                granjas[i] = granja.copy(silos = nuevosSilos)

                movimientos.add(
                    0,
                    Movimiento(
                        id = UUID.randomUUID().toString(),
                        siloId = siloId,
                        siloNombre = siloActual.nombre,
                        granjaNombre = granja.nombre,
                        tipo = tipo,
                        cantidadToneladas = cantidadTn,
                        fechaHora = LocalDateTime.now(),
                        responsable = responsable,
                        notas = notas
                    )
                )

                recalcularAlertas()
                realizado = true
                break
            }
        }
        return realizado
    }

    fun recalcularAlertas() {
        alertas.clear()
        for (granja in granjas) {
            for (silo in granja.silos) {
                if (silo.estado == EstadoSilo.CRITICO) {
                    alertas.add(
                        Alerta(
                            id = UUID.randomUUID().toString(),
                            siloId = silo.id,
                            siloNombre = silo.nombre,
                            granjaNombre = granja.nombre,
                            estado = EstadoSilo.CRITICO,
                            porcentajeActual = silo.porcentaje,
                            mensaje = "Nivel crítico en ${silo.nombre} (${granja.nombre}): ${silo.porcentaje}% de capacidad restante."
                        )
                    )
                } else if (silo.estado == EstadoSilo.ADVERTENCIA) {
                    alertas.add(
                        Alerta(
                            id = UUID.randomUUID().toString(),
                            siloId = silo.id,
                            siloNombre = silo.nombre,
                            granjaNombre = granja.nombre,
                            estado = EstadoSilo.ADVERTENCIA,
                            porcentajeActual = silo.porcentaje,
                            mensaje = "Nivel de advertencia en ${silo.nombre} (${granja.nombre}): ${silo.porcentaje}% de capacidad."
                        )
                    )
                }
            }
        }
    }
}
