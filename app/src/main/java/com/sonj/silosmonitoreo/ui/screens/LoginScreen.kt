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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sonj.silosmonitoreo.model.RolUsuario

@Composable
fun LoginScreen(
    onIngresar: (RolUsuario) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.OPERADOR) }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val intentarLogin = {
        if (usuario.isBlank() || contrasena.isBlank()) {
            errorMensaje = "Por favor ingrese usuario y contraseña"
        } else {
            errorMensaje = null
            onIngresar(rolSeleccionado)
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
            text = "Monitoreo de silos",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ingresa tus datos para continuar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Selecciona tu Rol:",
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
                    onClick = { rolSeleccionado = rol },
                    label = { Text(rol.etiqueta) }
                )
            }
        }

        OutlinedTextField(
            value = usuario,
            onValueChange = {
                usuario = it
                if (errorMensaje != null) errorMensaje = null
            },
            label = { Text("Usuario") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono usuario"
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        if (errorMensaje != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMensaje!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = intentarLogin,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
        ) {
            Text(text = "Ingresar")
        }
    }
}