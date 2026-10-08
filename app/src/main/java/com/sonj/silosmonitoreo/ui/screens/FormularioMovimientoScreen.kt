package com.sonj.silosmonitoreo.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sonj.silosmonitoreo.data.SilosRepository
import com.sonj.silosmonitoreo.model.TipoMovimiento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioMovimientoScreen(
    siloIdInicial: String? = null,
    onVolver: () -> Unit,
    onMovimientoGuardado: () -> Unit
) {
    val todosLosSilos = SilosRepository.granjas.flatMap { granja ->
        granja.silos.map { silo -> Pair(granja.nombre, silo) }
    }

    var siloSeleccionadoId by remember {
        mutableStateOf(siloIdInicial ?: todosLosSilos.firstOrNull()?.second?.id ?: "")
    }
    var tipoMovimiento by remember { mutableStateOf(TipoMovimiento.ENTRADA) }
    var cantidadText by remember { mutableStateOf("") }
    var responsableText by remember { mutableStateOf("") }
    var notasText by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Movimiento") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Registrar entrada o salida de grano",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Selección de Silo
            Text(
                text = "Selecciona el Silo:",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                todosLosSilos.forEach { (granjaNombre, silo) ->
                    FilterChip(
                        selected = (silo.id == siloSeleccionadoId),
                        onClick = { siloSeleccionadoId = silo.id },
                        label = { Text("$granjaNombre - ${silo.nombre} (${silo.tipoGrano})") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tipo de Movimiento
            Text(
                text = "Tipo de Operación:",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = (tipoMovimiento == TipoMovimiento.ENTRADA),
                    onClick = { tipoMovimiento = TipoMovimiento.ENTRADA },
                    leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
                    label = { Text("Carga / Entrada") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )
                FilterChip(
                    selected = (tipoMovimiento == TipoMovimiento.SALIDA),
                    onClick = { tipoMovimiento = TipoMovimiento.SALIDA },
                    leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null) },
                    label = { Text("Descarga / Salida") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cantidad en Toneladas
            OutlinedTextField(
                value = cantidadText,
                onValueChange = { cantidadText = it; mensajeError = null },
                label = { Text("Cantidad (Toneladas)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Responsable
            OutlinedTextField(
                value = responsableText,
                onValueChange = { responsableText = it; mensajeError = null },
                label = { Text("Nombre del Responsable / Operador") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notas opcionales
            OutlinedTextField(
                value = notasText,
                onValueChange = { notasText = it },
                label = { Text("Notas / Observaciones") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val cantidad = cantidadText.toIntOrNull()
                    if (siloSeleccionadoId.isBlank()) {
                        mensajeError = "Por favor selecciona un silo."
                    } else if (cantidad == null || cantidad <= 0) {
                        mensajeError = "Por favor ingresa una cantidad válida en toneladas."
                    } else if (responsableText.isBlank()) {
                        mensajeError = "Por favor ingresa el nombre del responsable."
                    } else {
                        val exito = SilosRepository.registrarMovimiento(
                            siloId = siloSeleccionadoId,
                            tipo = tipoMovimiento,
                            cantidadTn = cantidad,
                            responsable = responsableText.trim(),
                            notas = notasText.trim()
                        )
                        if (exito) {
                            onMovimientoGuardado()
                        } else {
                            mensajeError = "Error al registrar el movimiento."
                        }
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar Movimiento")
            }
        }
    }
}
