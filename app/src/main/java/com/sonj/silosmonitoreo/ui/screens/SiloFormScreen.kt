package com.sonj.silosmonitoreo.ui.screens

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonj.silosmonitoreo.ui.viewmodel.SiloFormViewModel

/** Crear (siloId = null) o editar un silo. Solo la usa el administrador. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiloFormScreen(
    siloId: String? = null,
    onVolver: () -> Unit,
    onOperacionExitosa: () -> Unit,
    viewModel: SiloFormViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var confirmarBorrado by remember { mutableStateOf(false) }

    LaunchedEffect(siloId) { viewModel.inicializar(siloId) }
    LaunchedEffect(estado.operacionExitosa) {
        if (estado.operacionExitosa) onOperacionExitosa()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (siloId == null) "Nuevo silo" else "Editar silo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Granja:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            viewModel.granjas.forEach { granja ->
                FilterChip(
                    selected = granja.id == estado.granjaId,
                    onClick = { viewModel.onGranjaSeleccionada(granja.id) },
                    label = { Text(granja.nombre) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            CampoSilo("Nombre del silo", estado.nombre, estado.errorNombre, viewModel::onNombreCambiado)
            Spacer(modifier = Modifier.height(12.dp))
            CampoSilo("Producto (ej. Trigo)", estado.producto, estado.errorProducto, viewModel::onProductoCambiado)
            Spacer(modifier = Modifier.height(12.dp))
            CampoSilo("Capacidad (Tn)", estado.capacidad, estado.errorCapacidad, viewModel::onCapacidadCambiada, true)
            Spacer(modifier = Modifier.height(12.dp))
            CampoSilo("Toneladas actuales", estado.toneladas, estado.errorToneladas, viewModel::onToneladasCambiadas, true)

            if (estado.mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = estado.mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.guardar() },
                enabled = !estado.guardando,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (estado.guardando) "Guardando..." else "Guardar silo")
            }

            if (siloId != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { confirmarBorrado = true },
                    enabled = !estado.guardando,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Eliminar silo", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("Eliminar silo") },
            text = { Text("Seguro que quieres eliminar ${estado.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarBorrado = false
                    viewModel.eliminar()
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun CampoSilo(
    etiqueta: String,
    valor: String,
    error: String?,
    onCambio: (String) -> Unit,
    numerico: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (numerico) KeyboardType.Number else KeyboardType.Text),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
}