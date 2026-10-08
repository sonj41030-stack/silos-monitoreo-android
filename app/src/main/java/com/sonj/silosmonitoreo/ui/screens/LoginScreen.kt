package com.sonj.silosmonitoreo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sonj.silosmonitoreo.data.auth.AuthRepository
import com.sonj.silosmonitoreo.model.RolUsuario

@Composable
fun LoginScreen(
    onIngresar: (RolUsuario) -> Unit,
    onCrearCuentaClick: () -> Unit
) {
    val context = LocalContext.current

    var usuarioEmail by remember { mutableStateOf("operador@silos.com") }
    var contrasena by remember { mutableStateOf("operador123") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.OPERADOR) }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val intentarLogin = {
        val resultado = AuthRepository.validarLogin(
            context = context,
            email = usuarioEmail,
            contrasena = contrasena,
            rol = rolSeleccionado
        )

        if (resultado.first) {
            errorMensaje = null
            onIngresar(rolSeleccionado)
        } else {
            errorMensaje = resultado.second
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Monitoreo de Silos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Autenticación segura con Token JWT y SQLite",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Selección de Rol
        Text(
            text = "Selecciona tu Rol para Iniciar Sesión:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
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
                    onClick = {
                        rolSeleccionado = rol
                        usuarioEmail = when (rol) {
                            RolUsuario.OPERADOR -> "operador@silos.com"
                            RolUsuario.ADMINISTRADOR -> "admin@silos.com"
                            RolUsuario.JEFATURA -> "jefe@silos.com"
                        }
                        contrasena = when (rol) {
                            RolUsuario.OPERADOR -> "operador123"
                            RolUsuario.ADMINISTRADOR -> "admin123"
                            RolUsuario.JEFATURA -> "jefe123"
                        }
                        errorMensaje = null
                    },
                    label = { Text(rol.etiqueta) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Correo Electrónico
        OutlinedTextField(
            value = usuarioEmail,
            onValueChange = {
                usuarioEmail = it
                if (errorMensaje != null) errorMensaje = null
            },
            label = { Text("Correo Electrónico") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Icono correo"
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
                if (errorMensaje != null) errorMensaje = null
            },
            label = { Text("Contraseña") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Icono contraseña"
                )
            },
            trailingIcon = {
                IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                    Icon(
                        imageVector = if (mostrarContrasena) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { intentarLogin() }
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMensaje != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMensaje!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = intentarLogin,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Ingresar (Validar Token)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onCrearCuentaClick,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Crear nueva cuenta (+18 años)")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ayuda con Cuentas Demo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Cuentas registradas por Rol:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Operador: operador@silos.com (clave: operador123)\n• Admin: admin@silos.com (clave: admin123)\n• Jefatura: jefe@silos.com (clave: jefe123)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
