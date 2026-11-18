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

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val errorConfirmacion: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val exito: Boolean = false
)

/** ViewModel del Registro: valida los 4 campos y crea la cuenta (el correo no puede repetirse). */
class RegistroViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    var uiState by mutableStateOf(RegistroUiState())
        private set

    fun onNombreChange(v: String) {
        uiState = uiState.copy(nombre = v, errorNombre = null, errorGeneral = null)
    }

    fun onCorreoChange(v: String) {
        uiState = uiState.copy(correo = v, errorCorreo = null, errorGeneral = null)
    }

    fun onPasswordChange(v: String) {
        uiState = uiState.copy(password = v, errorPassword = null, errorGeneral = null)
    }

    fun onConfirmacionChange(v: String) {
        uiState = uiState.copy(confirmacion = v, errorConfirmacion = null, errorGeneral = null)
    }

    fun registrar() {
        if (uiState.cargando) return
        val s = uiState
        val conErrores = s.copy(
            errorNombre = Validaciones.validarNombre(s.nombre),
            errorCorreo = Validaciones.validarCorreo(s.correo),
            errorPassword = Validaciones.validarPassword(s.password),
            errorConfirmacion = Validaciones.validarConfirmacion(s.password, s.confirmacion)
        )
        val hayErrores = listOf(
            conErrores.errorNombre, conErrores.errorCorreo,
            conErrores.errorPassword, conErrores.errorConfirmacion
        ).any { it != null }
        if (hayErrores) {
            uiState = conErrores
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            uiState = when (val r = usuarioRepository.registrar(s.nombre, s.correo, s.password)) {
                is ResultadoAuth.Exito -> uiState.copy(cargando = false, exito = true)
                // El único error posible aquí es el correo repetido: lo mostramos bajo ese campo
                is ResultadoAuth.Error -> uiState.copy(cargando = false, errorCorreo = r.mensaje)
            }
        }
    }
}
