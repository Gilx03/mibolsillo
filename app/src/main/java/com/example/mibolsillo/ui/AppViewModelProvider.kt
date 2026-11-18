package com.example.mibolsillo.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mibolsillo.MiBolsilloApplication
import com.example.mibolsillo.ui.auth.LoginViewModel
import com.example.mibolsillo.ui.auth.RegistroViewModel
import com.example.mibolsillo.ui.estadisticas.EstadisticasViewModel
import com.example.mibolsillo.ui.inicio.InicioViewModel
import com.example.mibolsillo.ui.metas.MetaFormViewModel
import com.example.mibolsillo.ui.metas.MetasViewModel
import com.example.mibolsillo.ui.presupuestos.PresupuestosViewModel
import com.example.mibolsillo.ui.movimientos.MovimientoFormViewModel
import com.example.mibolsillo.ui.movimientos.MovimientosViewModel
import com.example.mibolsillo.ui.perfil.PerfilViewModel
import com.example.mibolsillo.ui.splash.SplashViewModel

/**
 * Fábrica central de ViewModels.
 * Cada "initializer" explica cómo construir un ViewModel con los repositorios que necesita.
 * En una pantalla se usa así:  viewModel(factory = AppViewModelProvider.Factory)
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { MainViewModel(app().preferencias) }
        initializer { SplashViewModel(app().usuarioRepository) }
        initializer { LoginViewModel(app().usuarioRepository) }
        initializer { RegistroViewModel(app().usuarioRepository) }
        initializer { PerfilViewModel(app().usuarioRepository, app().preferencias, app().datosDemo) }
        initializer {
            InicioViewModel(
                app().usuarioRepository,
                app().movimientoRepository,
                app().presupuestoRepository,
                app().preferencias
            )
        }
        initializer {
            PresupuestosViewModel(app().presupuestoRepository, app().categoriaRepository, app().preferencias)
        }
        initializer { MetasViewModel(app().metaRepository, app().preferencias) }
        initializer { MetaFormViewModel(createSavedStateHandle(), app().metaRepository, app().preferencias) }
        initializer { EstadisticasViewModel(app().movimientoRepository, app().preferencias) }
        initializer {
            MovimientosViewModel(app().movimientoRepository, app().categoriaRepository, app().preferencias)
        }
        initializer {
            // createSavedStateHandle() entrega los argumentos de la ruta (el id del movimiento a editar)
            MovimientoFormViewModel(
                createSavedStateHandle(),
                app().movimientoRepository,
                app().categoriaRepository,
                app().preferencias
            )
        }
    }
}

/** Obtiene nuestra clase Application (el contenedor de dependencias) desde los CreationExtras. */
fun CreationExtras.app(): MiBolsilloApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MiBolsilloApplication
