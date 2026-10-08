package com.sonj.silosmonitoreo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sonj.silosmonitoreo.data.auth.AuthRepository
import com.sonj.silosmonitoreo.model.RolUsuario
import java.time.LocalDate

@Composable
fun CrearCuentaScreen(
    onCuentaCreada: (RolUsuario) -> Unit,
    onVolverLogin: () -> Unit
) {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var diaText by remember { mutableStateOf("15") }
    var mesText by remember { mutableStateOf("05") }
    var anioText by remember { mutableStateOf("2000") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.OPERADOR) }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val intentarRegistro = {
        val dia = diaText.toIntOrNull()
        val mes = mesText.toIntOrNull()
        val anio = anioText.toIntOrNull()

        if (dia == null || mes == null || anio == null || dia !in 1..31 || mes !in 1..12 || anio !in 1900..2026) {
            errorMensaje = "Por favor ingresa una fecha de nacimiento válida."
        } else {
            val fechaNac = LocalDate.of(anio, mes, dia)
            val resultado = AuthRepository.registrarNuevoUsuario(
                context = context,
                nombre = nombre,
                email = email,
                contrasena = contrasena,
                fechaNacimiento = fechaNac,
                rol = rolSeleccionado
            )

            if (resultado.first) {
                errorMensaje = null
                onCuentaCreada(rolSeleccionado)
            } else {
                errorMensaje = resultado.second
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crear nueva cuenta",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Registro con verificación de edad y rol",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nombre Completo
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; errorMensaje = null },
            label = { Text("Nombre Completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMensaje = null },
            label = { Text("Correo Electrónico (ej. usuario@silos.com)") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it; errorMensaje = null },
            label = { Text("Contraseña (min. 6 caracteres)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                    Icon(
                        imageVector = if (mostrarContrasena) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Mostrar u ocultar contraseña"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fecha de Nacimiento / Validación de Mayoría de Edad (>= 18 años)
        Text(
            text = "Fecha de Nacimiento (Mayor de 18 años):",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = diaText,
                onValueChange = { diaText = it; errorMensaje = null },
                label = { Text("Día") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = mesText,
                onValueChange = { mesText = it; errorMensaje = null },
                label = { Text("Mes") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = anioText,
                onValueChange = { anioText = it; errorMensaje = null },
                label = { Text("Año") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.5f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selección de Rol
        Text(
            text = "Rol a solicitar:",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RolUsuario.entries.forEach { rol ->
                FilterChip(
                    selected = (rol == rolSeleccionado),
                    onClick = { rolSeleccionado = rol },
                    label = { Text(rol.etiqueta) }
                )
            }
        }

        if (errorMensaje != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMensaje!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = intentarRegistro,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Registrarse y Autenticar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolverLogin,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Ya tengo cuenta - Iniciar Sesión")
        }
    }
}
