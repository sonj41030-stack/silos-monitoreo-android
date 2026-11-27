package com.sonj.silosmonitoreo

import com.sonj.silosmonitoreo.data.auth.ValidadorCuenta
import com.sonj.silosmonitoreo.ui.viewmodel.ValidadorMovimiento
import com.sonj.silosmonitoreo.ui.viewmodel.ValidadorSilo
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ValidadoresTest {

    // ---------- Movimiento ----------

    @Test
    fun movimiento_cantidad() {
        assertNotNull(ValidadorMovimiento.errorCantidad(""))
        assertNotNull(ValidadorMovimiento.errorCantidad("abc"))
        assertNotNull(ValidadorMovimiento.errorCantidad("1.5"))
        assertNotNull(ValidadorMovimiento.errorCantidad("0"))
        assertNotNull(ValidadorMovimiento.errorCantidad("1000"))
        assertNull(ValidadorMovimiento.errorCantidad("50"))
    }

    @Test
    fun movimiento_responsable_y_notas() {
        assertNotNull(ValidadorMovimiento.errorResponsable(""))
        assertNotNull(ValidadorMovimiento.errorResponsable("ab"))
        assertNull(ValidadorMovimiento.errorResponsable("Carlos"))
        assertNull(ValidadorMovimiento.errorNotas("ok"))
        assertNotNull(ValidadorMovimiento.errorNotas("x".repeat(101)))
    }

    // ---------- Cuenta ----------

    @Test
    fun cuenta_nombre_email_y_contrasena() {
        assertNotNull(ValidadorCuenta.errorNombre(""))
        assertNotNull(ValidadorCuenta.errorNombre("ab"))
        assertNull(ValidadorCuenta.errorNombre("Ana Perez"))
        assertNotNull(ValidadorCuenta.errorEmail(""))
        assertNotNull(ValidadorCuenta.errorEmail("sin_arroba"))
        assertNull(ValidadorCuenta.errorEmail("ana@silos.com"))
        assertNotNull(ValidadorCuenta.errorContrasena("123"))
        assertNull(ValidadorCuenta.errorContrasena("123456"))
    }

    @Test
    fun cuenta_fecha_de_nacimiento() {
        val anio = LocalDate.now().year
        assertNotNull(ValidadorCuenta.errorFechaNacimiento("a", "1", "2000"))
        assertNotNull(ValidadorCuenta.errorFechaNacimiento("1", "1", "1800"))
        assertNotNull(ValidadorCuenta.errorFechaNacimiento("31", "2", "2000"))
        assertNotNull(ValidadorCuenta.errorFechaNacimiento("1", "1", "${anio - 10}"))
        assertNull(ValidadorCuenta.errorFechaNacimiento("15", "5", "${anio - 30}"))
    }

    // ---------- Silo ----------

    @Test
    fun silo_nombre_y_producto() {
        assertNotNull(ValidadorSilo.errorNombre(""))
        assertNotNull(ValidadorSilo.errorNombre("ab"))
        assertNull(ValidadorSilo.errorNombre("Silo A1"))
        assertNotNull(ValidadorSilo.errorProducto(" "))
        assertNull(ValidadorSilo.errorProducto("Trigo"))
    }

    @Test
    fun silo_capacidad() {
        assertNotNull(ValidadorSilo.errorCapacidad(""))
        assertNotNull(ValidadorSilo.errorCapacidad("abc"))
        assertNotNull(ValidadorSilo.errorCapacidad("0"))
        assertNotNull(ValidadorSilo.errorCapacidad("10001"))
        assertNull(ValidadorSilo.errorCapacidad("150"))
    }

    @Test
    fun silo_toneladas_no_pueden_superar_la_capacidad() {
        assertNotNull(ValidadorSilo.errorToneladas("", "100"))
        assertNotNull(ValidadorSilo.errorToneladas("x", "100"))
        assertNotNull(ValidadorSilo.errorToneladas("-5", "100"))
        assertNotNull(ValidadorSilo.errorToneladas("150", "100"))
        assertNull(ValidadorSilo.errorToneladas("100", "100"))
        assertNull(ValidadorSilo.errorToneladas("50", ""))
    }
}