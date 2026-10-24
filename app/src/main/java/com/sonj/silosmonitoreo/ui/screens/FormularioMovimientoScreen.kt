package com.sonj.silosmonitoreo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonj.silosmonitoreo.model.TipoMovimiento
import com.sonj.silosmonitoreo.ui.viewmodel.FormularioMovimientoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioMovimientoScreen(
    siloIdInicial: String? = null,
    onVolver: () -> Unit,
    onMovimientoGuardado: () -> Unit,
    viewModel: FormularioMovimientoViewModel = viewModel()
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsState()
    val todosLosSilos = viewModel.obtenerSilos()

    // Al abrir la pantalla, el ViewModel deja seleccionado el silo inicial
    LaunchedEffect(siloIdInicial) {
        viewModel.inicializarSilo(siloIdInicial)
    }

    // Cuando el ViewModel avisa que el movimiento se guardó, volvemos atrás
    LaunchedEffect(estado.movimientoGuardado) {
        if (estado.movimientoGuardado) {
            onMovimientoGuardado()
        }
    }

    // --- CAMARA (recurso nativo) ---
    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.onFotoCapturada(bitmap)
        }
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            viewModel.onPermisoConcedido()
            lanzadorCamara.launch(null)
        } else {
            viewModel.onPermisoDenegado()
        }
    }

    val tomarFoto = {
        val permisoConcedido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (permisoConcedido) {
            lanzadorCamara.launch(null)
        } else {
            lanzadorPermiso.launch(Manifest.permission.CAMERA)
        }
    }

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
                        selected = (silo.id == estado.siloSeleccionadoId),
                        onClick = { viewModel.onSiloSeleccionado(silo.id) },
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
                    selected = (estado.tipoMovimiento == TipoMovimiento.ENTRADA),
                    onClick = { viewModel.onTipoCambiado(TipoMovimiento.ENTRADA) },
                    leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
                    label = { Text("Carga / Entrada") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )
                FilterChip(
                    selected = (estado.tipoMovimiento == TipoMovimiento.SALIDA),
                    onClick = { viewModel.onTipoCambiado(TipoMovimiento.SALIDA) },
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
                value = estado.cantidad,
                onValueChange = { viewModel.onCantidadCambiada(it) },
                label = { Text("Cantidad (Toneladas)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Responsable
            OutlinedTextField(
                value = estado.responsable,
                onValueChange = { viewModel.onResponsableCambiado(it) },
                label = { Text("Nombre del Responsable / Operador") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notas opcionales
            OutlinedTextField(
                value = estado.notas,
                onValueChange = { viewModel.onNotasCambiadas(it) },
                label = { Text("Notas / Observaciones") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Foto de evidencia (camara)
            Text(
                text = "Foto de evidencia (opcional):",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (estado.fotoEvidencia != null) {
                Image(
                    bitmap = estado.fotoEvidencia!!.asImageBitmap(),
                    contentDescription = "Foto de evidencia del movimiento",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = tomarFoto,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (estado.fotoEvidencia == null) "Tomar foto" else "Repetir foto")
            }

            if (estado.mensajePermiso != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = estado.mensajePermiso!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (estado.mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = estado.mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.guardarMovimiento() },
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