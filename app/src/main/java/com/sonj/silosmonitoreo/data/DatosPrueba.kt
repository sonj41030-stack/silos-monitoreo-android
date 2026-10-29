package com.sonj.silosmonitoreo.data

import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.Movimiento
import com.sonj.silosmonitoreo.model.Silo
import com.sonj.silosmonitoreo.model.TipoMovimiento
import java.time.LocalDateTime

object DatosPrueba {

    // Granjas y Plantas con nombres y operaciones reales de Ariztía en Melipilla
    val granjasDePrueba = listOf(
        Granja(
            id = "g1",
            nombre = "Planta Tarapacá (Ariztía)",
            ubicacion = "Melipilla — Sector Tarapacá, Alimentos Balanceados",
            silos = listOf(
                Silo("s1", "Silo A1", 80, "Maíz", 150, "Plantel Broiler Ariztía (Pollos)"),
                Silo("s2", "Silo A2", 25, "Soya", 120, "Planta Alimentos Melipilla (Proteína)"),
                Silo("s3", "Silo A3", 10, "Trigo", 100, "Plantel Pavos Ariztía (Crianza)")
            )
        ),
        Granja(
            id = "g2",
            nombre = "Sector Puangue (Ariztía)",
            ubicacion = "Melipilla — Valle de Puangue, Centro Acopio",
            silos = listOf(
                Silo("s4", "Silo B1", 90, "Girasol", 200, "Planta Cecinas y Subproductos Ariztía"),
                Silo("s5", "Silo B2", 12, "Cebada", 150, "Plantel Crianza Tarapacá")
            )
        ),
        Granja(
            id = "g3",
            nombre = "Casa Matriz Los Carrera",
            ubicacion = "Melipilla — Av. Los Carrera 444, Centro Operativo",
            silos = listOf(
                Silo("s6", "Silo C1", 50, "Sorgo", 180, "Mezcladora de Forraje Puangue"),
                Silo("s7", "Silo C2", 28, "Maíz Blanco", 160, "Línea Alimentos Integrados Ariztía")
            )
        )
    )

    // Movimientos de prueba iniciales (se cargan en SQLite la primera vez)
    val movimientosDePrueba = listOf(
        Movimiento(
            id = "m1",
            siloId = "s1",
            siloNombre = "Silo 1",
            granjaNombre = "Planta Tarapacá",
            tipo = TipoMovimiento.ENTRADA,
            cantidadToneladas = 30,
            fechaHora = LocalDateTime.now().minusHours(4),
            responsable = "Carlos Operador",
            notas = "Carga rutinaria de maíz para alimento balanceado"
        ),
        Movimiento(
            id = "m2",
            siloId = "s3",
            siloNombre = "Silo 3",
            granjaNombre = "Planta Tarapacá",
            tipo = TipoMovimiento.SALIDA,
            cantidadToneladas = 45,
            fechaHora = LocalDateTime.now().minusDays(1),
            responsable = "Ana Administradora",
            notas = "Despacho a planta de procesamiento Ariztía"
        ),
        Movimiento(
            id = "m3",
            siloId = "s5",
            siloNombre = "Silo 2",
            granjaNombre = "Sector Puangue",
            tipo = TipoMovimiento.SALIDA,
            cantidadToneladas = 50,
            fechaHora = LocalDateTime.now().minusHours(12),
            responsable = "Roberto Jefe",
            notas = "Transilaje a granero de distribución"
        )
    )
}

