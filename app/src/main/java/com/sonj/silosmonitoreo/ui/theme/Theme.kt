package com.sonj.silosmonitoreo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = Texto,
    onPrimary = Superficie,
    secondary = TextoSecundario,
    tertiary = Acento,
    background = Fondo,
    surface = Superficie,
    surfaceVariant = Pista,
    onBackground = Texto,
    onSurface = Texto,
    onSurfaceVariant = TextoSecundario
)

private val EsquemaOscuro = darkColorScheme(
    primary = TextoOscuro,
    onPrimary = FondoOscuro,
    secondary = TextoSecundarioOscuro,
    tertiary = Acento,
    background = FondoOscuro,
    surface = SuperficieOscura,
    surfaceVariant = PistaOscura,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro,
    onSurfaceVariant = TextoSecundarioOscuro
)

@Composable
fun SilosMonitoreoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}