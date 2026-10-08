package com.sonj.silosmonitoreo.data

import com.sonj.silosmonitoreo.model.Granja
import com.sonj.silosmonitoreo.model.Silo

object DatosPrueba {
    val granjasDePrueba = listOf(
        Granja(
            id = "g1",
            nombre = "Granja Norte",
            ubicacion = "Sector A - Campo Principal",
            silos = listOf(
                Silo("s1", "Silo 1", 80, "Maíz", 150),
                Silo("s2", "Silo 2", 25, "Soya", 120),
                Silo("s3", "Silo 3", 10, "Trigo", 100)
            )
        ),
        Granja(
            id = "g2",
            nombre = "Granja Sur",
            ubicacion = "Sector B - Valle Sur",
            silos = listOf(
                Silo("s4", "Silo 1", 90, "Girasol", 200),
                Silo("s5", "Silo 2", 12, "Cebada", 150)
            )
        ),
        Granja(
            id = "g3",
            nombre = "Granja Este",
            ubicacion = "Sector C - Pampa Este",
            silos = listOf(
                Silo("s6", "Silo 1", 50, "Sorgo", 180),
                Silo("s7", "Silo 2", 28, "Maíz Blanco", 160)
            )
        )
    )
}
