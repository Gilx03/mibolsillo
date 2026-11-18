package com.example.mibolsillo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mibolsillo.ui.navigation.MiBolsilloNavHost
import com.example.mibolsillo.ui.navigation.Rutas
import com.example.mibolsillo.ui.navigation.irAPestana
import com.example.mibolsillo.ui.navigation.itemsBarraInferior

/**
 * Composable raíz: un Scaffold con la barra de navegación inferior y el NavHost dentro.
 * La barra inferior solo se muestra en las 5 pestañas principales.
 */
@Composable
fun MiBolsilloApp() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val rutaActual = destinoActual?.route

    val mostrarBarra = itemsBarraInferior.any { it.ruta == rutaActual }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = mostrarBarra,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NavigationBar {
                    itemsBarraInferior.forEach { item ->
                        val seleccionado = destinoActual?.hierarchy?.any { it.route == item.ruta } == true
                        NavigationBarItem(
                            selected = seleccionado,
                            onClick = { navController.irAPestana(item.ruta) },
                            icon = {
                                Icon(
                                    if (seleccionado) item.iconoSeleccionado else item.iconoNormal,
                                    contentDescription = item.etiqueta
                                )
                            },
                            label = { Text(item.etiqueta, maxLines = 1) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // El Splash ocupa toda la pantalla (detrás de las barras del sistema); el resto respeta los márgenes.
        val modificador = if (rutaActual == Rutas.SPLASH) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        }
        MiBolsilloNavHost(navController = navController, modifier = modificador)
    }
}
