package com.example.mibolsillo.ui.splash

import androidx.lifecycle.ViewModel
import com.example.mibolsillo.data.repository.UsuarioRepository

/** Decide a dónde ir después del Splash. */
class SplashViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    /** true si hay una sesión guardada y el usuario sigue existiendo en la base de datos. */
    suspend fun haySesionActiva(): Boolean = usuarioRepository.obtenerSesionValida() != null
}
