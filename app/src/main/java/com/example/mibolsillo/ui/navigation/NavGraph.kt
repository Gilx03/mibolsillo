package com.example.mibolsillo.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mibolsillo.ui.auth.LoginScreen
import com.example.mibolsillo.ui.auth.RegistroScreen
import com.example.mibolsillo.ui.consejos.ConsejosScreen
import com.example.mibolsillo.ui.estadisticas.EstadisticasScreen
import com.example.mibolsillo.ui.inicio.InicioScreen
import com.example.mibolsillo.ui.metas.MetaFormScreen
import com.example.mibolsillo.ui.metas.MetaFormViewModel
import com.example.mibolsillo.ui.metas.MetasScreen
import com.example.mibolsillo.ui.presupuestos.PresupuestosScreen
import com.example.mibolsillo.ui.movimientos.MovimientoFormScreen
import com.example.mibolsillo.ui.movimientos.MovimientoFormViewModel
import com.example.mibolsillo.ui.movimientos.MovimientosScreen
import com.example.mibolsillo.ui.perfil.PerfilScreen
import com.example.mibolsillo.ui.splash.SplashScreen

/**
 * Mapa de navegación de la app: qué pantalla corresponde a cada ruta
 * y cómo se pasa de una a otra.
 *
 * Flujo: Splash -> (Login <-> Registro) -> Inicio [barra inferior]
 * Al entrar a Inicio se borra Splash/Login del historial, para que "Atrás" no regrese a ellos.
 */
@Composable
fun MiBolsilloNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Rutas.SPLASH,
        modifier = modifier,
        // Animaciones: las pestañas cambian con un fundido; las pantallas secundarias
        // (formularios, Presupuestos, Consejos) entran deslizándose desde la derecha.
        enterTransition = {
            if (targetState.destination.route in rutasSinDeslizar) fadeIn(tween(250))
            else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeIn(tween(300))
        },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(250)) },
        popExitTransition = {
            if (initialState.destination.route in rutasSinDeslizar) fadeOut(tween(200))
            else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeOut(tween(300))
        }
    ) {
        composable(Rutas.SPLASH) {
            SplashScreen(
                onIrAInicio = { navController.irAInicioLimpiando(Rutas.SPLASH) },
                onIrALogin = {
                    navController.navigate(Rutas.LOGIN) { popUpTo(Rutas.SPLASH) { inclusive = true } }
                }
            )
        }

        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginExitoso = { navController.irAInicioLimpiando(Rutas.LOGIN) },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onRegistroExitoso = { navController.irAInicioLimpiando(Rutas.LOGIN) },
                onIrALogin = { navController.popBackStack() }
            )
        }

        // ---------- Pestañas principales ----------
        composable(Rutas.INICIO) {
            InicioScreen(
                onAgregarMovimiento = { navController.navigate(Rutas.movimientoForm()) },
                onEditarMovimiento = { id -> navController.navigate(Rutas.movimientoForm(id)) },
                onVerMovimientos = { navController.irAPestana(Rutas.MOVIMIENTOS) },
                onPresupuestos = { navController.navigate(Rutas.PRESUPUESTOS) },
                onConsejos = { navController.navigate(Rutas.CONSEJOS) }
            )
        }
        composable(Rutas.MOVIMIENTOS) {
            MovimientosScreen(
                onAgregar = { navController.navigate(Rutas.movimientoForm()) },
                onEditar = { id -> navController.navigate(Rutas.movimientoForm(id)) }
            )
        }
        composable(Rutas.ESTADISTICAS) { EstadisticasScreen() }
        composable(Rutas.METAS) {
            MetasScreen(
                onNuevaMeta = { navController.navigate(Rutas.metaForm()) },
                onEditarMeta = { id -> navController.navigate(Rutas.metaForm(id)) }
            )
        }
        composable(Rutas.PERFIL) {
            PerfilScreen(onSesionCerrada = { navController.irALoginLimpiandoTodo() })
        }

        // ---------- Pantallas secundarias ----------
        composable(
            route = Rutas.MOVIMIENTO_FORM,
            arguments = listOf(
                navArgument(MovimientoFormViewModel.ARG_ID) {
                    type = NavType.LongType
                    defaultValue = -1L // -1 = movimiento nuevo
                }
            )
        ) {
            MovimientoFormScreen(onTerminado = { navController.popBackStack() })
        }
        composable(
            route = Rutas.META_FORM,
            arguments = listOf(
                navArgument(MetaFormViewModel.ARG_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            MetaFormScreen(onTerminado = { navController.popBackStack() })
        }
        composable(Rutas.PRESUPUESTOS) {
            PresupuestosScreen(onAtras = { navController.popBackStack() })
        }
        composable(Rutas.CONSEJOS) {
            ConsejosScreen(onAtras = { navController.popBackStack() })
        }
    }
}

/** Rutas que cambian con fundido (pestañas y flujo de inicio de sesión). */
private val rutasSinDeslizar = itemsBarraInferior.map { it.ruta } + listOf(Rutas.SPLASH, Rutas.LOGIN)

/** Va a Inicio quitando del historial todo hasta [rutaABorrar] (incluida). */
private fun NavHostController.irAInicioLimpiando(rutaABorrar: String) {
    navigate(Rutas.INICIO) {
        popUpTo(rutaABorrar) { inclusive = true }
        launchSingleTop = true
    }
}

/** Al cerrar sesión: vacía TODO el historial y deja solo el Login. */
fun NavHostController.irALoginLimpiandoTodo() {
    navigate(Rutas.LOGIN) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

/** Navegación entre pestañas de la barra inferior conservando el estado de cada una. */
fun NavHostController.irAPestana(ruta: String) {
    navigate(ruta) {
        popUpTo(Rutas.INICIO) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
