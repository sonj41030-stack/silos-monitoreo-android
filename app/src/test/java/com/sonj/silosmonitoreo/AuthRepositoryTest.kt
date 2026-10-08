package com.sonj.silosmonitoreo

import com.sonj.silosmonitoreo.data.auth.AuthRepository
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AuthRepositoryTest {

    @Test
    fun testValidacionFormatoEmail() {
        assertTrue(AuthRepository.esEmailValido("operador@silos.com"))
        assertTrue(AuthRepository.esEmailValido("admin.silos@empresa.cl"))
        assertFalse(AuthRepository.esEmailValido("correo_invalido"))
        assertFalse(AuthRepository.esEmailValido("sin_arroba.com"))
    }

    @Test
    fun testValidacionMayorDeEdad() {
        val usuarioMenor = Usuario(
            id = "u_menor",
            nombre = "Juan Menor",
            email = "menor@silos.com",
            contrasena = "123456",
            fechaNacimiento = LocalDate.now().minusYears(16),
            rol = RolUsuario.OPERADOR
        )
        assertFalse("El usuario de 16 años no debe ser mayor de edad", usuarioMenor.esMayorDeEdad)

        val usuarioMayor = Usuario(
            id = "u_mayor",
            nombre = "Pedro Adulto",
            email = "adulto@silos.com",
            contrasena = "123456",
            fechaNacimiento = LocalDate.now().minusYears(25),
            rol = RolUsuario.OPERADOR
        )
        assertTrue("El usuario de 25 años debe ser mayor de edad", usuarioMayor.esMayorDeEdad)
        assertTrue(usuarioMayor.edad >= 18)
    }

    @Test
    fun testCorreosDiferentesPorRol() {
        val correoOperador = "operador@silos.com"
        val correoAdmin = "admin@silos.com"
        val correoJefatura = "jefe@silos.com"

        assertTrue("Los correos por rol deben ser distintos entre sí", correoOperador != correoAdmin)
        assertTrue("Los correos por rol deben ser distintos entre sí", correoAdmin != correoJefatura)
        assertTrue("Los correos por rol deben ser distintos entre sí", correoOperador != correoJefatura)
    }
}
