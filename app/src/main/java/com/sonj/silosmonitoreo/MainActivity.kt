package com.sonj.silosmonitoreo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sonj.silosmonitoreo.ui.components.TarjetaSilo
import com.sonj.silosmonitoreo.ui.theme.SilosMonitoreoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SilosMonitoreoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Granja Los Pinos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Silos",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        TarjetaSilo(nombre = "Silo A1", porcentaje = 72)
                        TarjetaSilo(nombre = "Silo A2", porcentaje = 24)
                        TarjetaSilo(nombre = "Silo B1", porcentaje = 9)
                    }
                }
            }
        }
    }
}