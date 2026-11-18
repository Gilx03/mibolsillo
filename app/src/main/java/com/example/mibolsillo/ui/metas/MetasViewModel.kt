package com.example.mibolsillo.ui.metas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mibolsillo.data.local.entity.MetaAhorro
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import com.example.mibolsillo.data.repository.MetaRepository
import com.example.mibolsillo.util.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MetasUiState(
    val metas: List<MetaAhorro> = emptyList(),
    val cargando: Boolean = true
) {
    val totalAhorrado: Double get() = metas.sumOf { it.montoActual }
    val totalObjetivo: Double get() = metas.sumOf { it.montoObjetivo }
    val completadas: Int get() = metas.count { it.completada }
}

/** Diálogo para registrar un aporte a una meta. */
data class FormAporte(val meta: MetaAhorro, val montoTexto: String = "", val error: String? = null)

@OptIn(ExperimentalCoroutinesApi::class)
class MetasViewModel(
    private val metaRepository: MetaRepository,
    private val preferencias: PreferenciasRepository
) : ViewModel() {

    val uiState: StateFlow<MetasUiState> = preferencias.usuarioIdSesion
        .filterNotNull()
        .flatMapLatest { metaRepository.observar(it) }
        .map { MetasUiState(it, cargando = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MetasUiState())

    var formAporte by mutableStateOf<FormAporte?>(null)
        private set

    /** Nombre de la meta que se acaba de completar (para mostrar la felicitación) o null. */
    var metaCompletada by mutableStateOf<String?>(null)
        private set

    fun abrirAporte(meta: MetaAhorro) {
        formAporte = FormAporte(meta)
    }

    fun cerrarAporte() {
        formAporte = null
    }

    fun onMontoAporte(texto: String) {
        formAporte = formAporte?.copy(
            montoTexto = texto.filter { it.isDigit() || it == '.' || it == ',' }.take(12),
            error = null
        )
    }

    fun registrarAporte() {
        val f = formAporte ?: return
        val error = Validaciones.validarMonto(f.montoTexto)
        if (error != null) {
            formAporte = f.copy(error = error)
            return
        }
        viewModelScope.launch {
            val usuarioId = preferencias.usuarioIdSesion.first() ?: return@launch
            val antes = f.meta
            val despues = metaRepository.aportar(antes.id, usuarioId, Validaciones.parsearMonto(f.montoTexto)!!)
            formAporte = null
            // Solo felicitamos en el momento en que la meta PASA a estar completa
            if (!antes.completada && despues?.completada == true) {
                metaCompletada = despues.nombre
            }
        }
    }

    fun felicitacionVista() {
        metaCompletada = null
    }

    fun eliminar(meta: MetaAhorro) {
        viewModelScope.launch {
            val usuarioId = preferencias.usuarioIdSesion.first() ?: return@launch
            metaRepository.eliminar(meta.id, usuarioId)
        }
    }
}
