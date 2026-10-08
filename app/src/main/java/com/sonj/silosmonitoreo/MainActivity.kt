package com.sonj.silosmonitoreo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.navigation.AppDestinations
import com.sonj.silosmonitoreo.ui.screens.DashboardJefaturaScreen
import com.sonj.silosmonitoreo.ui.screens.DetalleSiloScreen
import com.sonj.silosmonitoreo.ui.screens.FormularioMovimientoScreen
import com.sonj.silosmonitoreo.ui.screens.HistorialMovimientosScreen
import com.sonj.silosmonitoreo.ui.screens.ListaSilosScreen
import com.sonj.silosmonitoreo.ui.screens.LoginScreen
import com.sonj.silosmonitoreo.ui.screens.PantallaAlertasScreen
import com.sonj.silosmonitoreo.ui.screens.SeleccionRolScreen
import com.sonj.silosmonitoreo.ui.theme.SilosMonitoreoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SilosMonitoreoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = AppDestinations.LOGIN,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 1. LOGIN
                        composable(AppDestinations.LOGIN) {
                            LoginScreen(
                                onIngresar = { rol ->
                                    navController.navigate("${AppDestinations.LISTA_SILOS}/${rol.name}") {
                                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. SELECCION DE ROL
                        composable(
                            route = "${AppDestinations.SELECCION_ROL}/{rolInicial}",
                            arguments = listOf(navArgument("rolInicial") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolInicialName = backStackEntry.arguments?.getString("rolInicial")
                            val rolInicial = RolUsuario.entries.firstOrNull { it.name == rolInicialName } ?: RolUsuario.OPERADOR
                            SeleccionRolScreen(
                                rolInicial = rolInicial,
                                onRolConfirmado = { rolConfirmado ->
                                    navController.navigate("${AppDestinations.LISTA_SILOS}/${rolConfirmado.name}") {
                                        popUpTo(AppDestinations.SELECCION_ROL) { inclusive = true }
                                    }
                                },
                                onVolverLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 3. LISTA DE GRANJAS Y SILOS
                        composable(
                            route = "${AppDestinations.LISTA_SILOS}/{rolName}",
                            arguments = listOf(navArgument("rolName") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolName = backStackEntry.arguments?.getString("rolName")
                            val rol = RolUsuario.entries.firstOrNull { it.name == rolName } ?: RolUsuario.OPERADOR
                            ListaSilosScreen(
                                rol = rol,
                                onSiloClick = { siloId ->
                                    navController.navigate("${AppDestinations.DETALLE_SILO}/$siloId/${rol.name}")
                                },
                                onRegistrarMovimientoClick = {
                                    navController.navigate(AppDestinations.REGISTRO_MOVIMIENTO)
                                },
                                onVerHistorialClick = {
                                    navController.navigate(AppDestinations.HISTORIAL_MOVIMIENTOS)
                                },
                                onVerAlertasClick = {
                                    navController.navigate(AppDestinations.PANTALLA_ALERTAS)
                                },
                                onVerDashboardJefaturaClick = {
                                    navController.navigate(AppDestinations.DASHBOARD_JEFATURA)
                                },
                                onCambiarRol = {
                                    navController.navigate("${AppDestinations.SELECCION_ROL}/${rol.name}")
                                },
                                onCerrarSesion = {
                                    navController.navigate(AppDestinations.LOGIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 4. DETALLE DE SILO
                        composable(
                            route = "${AppDestinations.DETALLE_SILO}/{siloId}/{rolName}",
                            arguments = listOf(
                                navArgument("siloId") { type = NavType.StringType },
                                navArgument("rolName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val siloId = backStackEntry.arguments?.getString("siloId") ?: ""
                            val rolName = backStackEntry.arguments?.getString("rolName")
                            val rol = RolUsuario.entries.firstOrNull { it.name == rolName } ?: RolUsuario.OPERADOR
                            DetalleSiloScreen(
                                siloId = siloId,
                                rol = rol,
                                onVolver = { navController.popBackStack() },
                                onRegistrarMovimiento = { id ->
                                    navController.navigate("${AppDestinations.REGISTRO_MOVIMIENTO}?siloId=$id")
                                },
                                onVerHistorialSilo = { id ->
                                    navController.navigate("${AppDestinations.HISTORIAL_MOVIMIENTOS}?siloId=$id")
                                }
                            )
                        }

                        // 5. REGISTRO DE MOVIMIENTO
                        composable(
                            route = "${AppDestinations.REGISTRO_MOVIMIENTO}?siloId={siloId}",
                            arguments = listOf(navArgument("siloId") {
                                type = NavType.StringType
                                nullable = true
                            })
                        ) { backStackEntry ->
                            val siloId = backStackEntry.arguments?.getString("siloId")
                            FormularioMovimientoScreen(
                                siloIdInicial = siloId,
                                onVolver = { navController.popBackStack() },
                                onMovimientoGuardado = { navController.popBackStack() }
                            )
                        }

                        // 6. HISTORIAL DE MOVIMIENTOS
                        composable(
                            route = "${AppDestinations.HISTORIAL_MOVIMIENTOS}?siloId={siloId}",
                            arguments = listOf(navArgument("siloId") {
                                type = NavType.StringType
                                nullable = true
                            })
                        ) { backStackEntry ->
                            val siloId = backStackEntry.arguments?.getString("siloId")
                            HistorialMovimientosScreen(
                                siloIdFiltro = siloId,
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        // 7. PANTALLA DE ALERTAS
                        composable(AppDestinations.PANTALLA_ALERTAS) {
                            PantallaAlertasScreen(
                                onVolver = { navController.popBackStack() },
                                onVerSilo = { siloId ->
                                    navController.navigate("${AppDestinations.DETALLE_SILO}/$siloId/${RolUsuario.ADMINISTRADOR.name}")
                                }
                            )
                        }

                        // 8. DASHBOARD JEFATURA
                        composable(AppDestinations.DASHBOARD_JEFATURA) {
                            DashboardJefaturaScreen(
                                onVolver = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
