package com.example.mibolsillo.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mibolsillo.data.repository.ResultadoAuth
import com.example.mibolsillo.data.repository.UsuarioRepository
import com.example.mibolsillo.util.Validaciones
import kotlinx.coroutines.launch

/** Todo lo que la pantalla de Login necesita dibujar. */
data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val errorGeneral: String? = null, // p. ej. "Contraseña incorrecta"
    val cargando: Boolean = false,
    val exito: Boolean = false
)

/**
 * ViewModel del Login (patrón MVVM):
 * la pantalla solo dibuja [uiState] y avisa de eventos (escribir, pulsar botón);
 * la lógica de validación y el acceso a datos ocurren aquí.
 */
class LoginViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    // Al escribir, borramos el error de ese campo para no molestar al usuario.
    fun onCorreoChange(valor: String) {
        uiState = uiState.copy(correo = valor, errorCorreo = null, errorGeneral = null)
    }

    fun onPasswordChange(valor: String) {
        uiState = uiState.copy(password = valor, errorPassword = null, errorGeneral = null)
    }

    fun iniciarSesion() {
        if (uiState.cargando) return

        // 1) Validación local de los campos
        val errorCorreo = Validaciones.validarCorreo(uiState.correo)
        val errorPassword = Validaciones.validarPassword(uiState.password)
        if (errorCorreo != null || errorPassword != null) {
            uiState = uiState.copy(errorCorreo = errorCorreo, errorPassword = errorPassword)
            return
        }

        // 2) Comprobación contra la base de datos (en segundo plano, con corrutinas)
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            uiState = when (val r = usuarioRepository.iniciarSesion(uiState.correo, uiState.password)) {
                is ResultadoAuth.Exito -> uiState.copy(cargando = false, exito = true)
                is ResultadoAuth.Error -> uiState.copy(cargando = false, errorGeneral = r.mensaje)
            }
        }
    }
}
