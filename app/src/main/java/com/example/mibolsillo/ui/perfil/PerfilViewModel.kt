package com.example.mibolsillo.ui.perfil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mibolsillo.data.demo.DatosDemo
import com.example.mibolsillo.data.local.entity.Usuario
import com.example.mibolsillo.data.preferencias.ModoTema
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import com.example.mibolsillo.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel del Perfil: datos del usuario, preferencias (moneda y tema),
 * datos de demostración y cierre de sesión.
 */
class PerfilViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val preferencias: PreferenciasRepository,
    private val datosDemo: DatosDemo
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = usuarioRepository.usuarioActual.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    val moneda: StateFlow<String> = preferencias.moneda.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), PreferenciasRepository.MONEDA_POR_DEFECTO
    )

    val modoTema: StateFlow<ModoTema> = preferencias.modoTema.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), ModoTema.SISTEMA
    )

    var mensaje by mutableStateOf<String?>(null)
        private set

    var cargandoDemo by mutableStateOf(false)
        private set

    /** Al guardar en DataStore, MainActivity recibe el cambio y toda la app se actualiza. */
    fun cambiarMoneda(simbolo: String) {
        viewModelScope.launch { preferencias.guardarMoneda(simbolo) }
    }

    fun cambiarTema(modo: ModoTema) {
        viewModelScope.launch { preferencias.guardarModoTema(modo) }
    }

    fun cargarDatosDemo() {
        if (cargandoDemo) return
        viewModelScope.launch {
            cargandoDemo = true
            val usuarioId = preferencias.usuarioIdSesion.first()
            if (usuarioId != null) {
                mensaje = if (datosDemo.cargar(usuarioId)) {
                    "Datos de demostración cargados. ¡Revisa Inicio y Estadísticas!"
                } else {
                    "Solo se pueden cargar en una cuenta sin movimientos"
                }
            }
            cargandoDemo = false
        }
    }

    fun mensajeMostrado() {
        mensaje = null
    }

    /** Borra la sesión y luego avisa a la pantalla para que navegue al Login. */
    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            usuarioRepository.cerrarSesion()
            alTerminar()
        }
    }
}
