package com.example.mibolsillo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mibolsillo.data.preferencias.ModoTema
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.MainViewModel
import com.example.mibolsillo.ui.MiBolsilloApp
import com.example.mibolsillo.ui.components.LocalMoneda
import com.example.mibolsillo.ui.theme.MiBolsilloTheme

/** Única Activity de la app: todas las pantallas son Composables dentro de ella. */
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val modoTema by mainViewModel.modoTema.collectAsStateWithLifecycle()
            val oscuro = when (modoTema) {
                ModoTema.SISTEMA -> isSystemInDarkTheme()
                ModoTema.CLARO -> false
                ModoTema.OSCURO -> true
            }

            // Ajusta el color de los iconos de la barra de estado según el tema elegido en la app.
            LaunchedEffect(oscuro) {
                val estilo = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { oscuro }
                enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
            }

            val moneda by mainViewModel.moneda.collectAsStateWithLifecycle()

            MiBolsilloTheme(darkTheme = oscuro) {
                // A partir de aquí, cualquier pantalla puede leer la moneda con LocalMoneda.current
                CompositionLocalProvider(LocalMoneda provides moneda) {
                    MiBolsilloApp()
                }
            }
        }
    }
}
