package com.sonj.silosmonitoreo.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.sonj.silosmonitoreo.data.db.LocalDatabaseManager
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

    val movimientos = mutableStateListOf<Movimiento>()

    val alertas = mutableStateListOf<Alerta>()

    // ── Inicialización con la base de datos ──────────────────────────────

    /**
     * Carga los movimientos desde SQLite al iniciar la app.
     * Debe llamarse desde MainActivity o desde el primer ViewModel que se crea.
     */
    fun cargarMovimientosDesdeDB(context: Context) {
        val db = LocalDatabaseManager.getInstance(context)
        val movimientosGuardados = db.obtenerTodosLosMovimientos()
        movimientos.clear()
        if (movimientosGuardados.isEmpty()) {
            // Si la BD está vacía, cargamos los datos de prueba iniciales
            movimientos.addAll(DatosPrueba.movimientosDePrueba)
            // Y los persistimos en la BD para la próxima vez
            movimientosGuardados // no hace nada, solo continúa
        } else {
            movimientos.addAll(movimientosGuardados)
        }
        recalcularAlertas()
    }

    // ── Operaciones ──────────────────────────────────────────────────────

    fun obtenerSilo(siloId: String): Pair<Granja, Silo>? {
        for (g in granjas) {
            val s = g.silos.firstOrNull { it.id == siloId }
            if (s != null) return Pair(g, s)
        }
        return null
    }

    fun registrarMovimiento(
        context: Context,
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

                val nuevoMovimiento = Movimiento(
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

                // Persistir en SQLite
                val db = LocalDatabaseManager.getInstance(context)
                db.guardarMovimiento(nuevoMovimiento)

                // Actualizar lista en memoria (reactiva)
                movimientos.add(0, nuevoMovimiento)

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

