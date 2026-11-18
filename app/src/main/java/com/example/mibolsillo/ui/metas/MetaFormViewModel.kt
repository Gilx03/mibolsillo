package com.example.mibolsillo.ui.metas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mibolsillo.data.local.entity.MetaAhorro
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import com.example.mibolsillo.data.repository.MetaRepository
import com.example.mibolsillo.util.Fechas
import com.example.mibolsillo.util.Validaciones
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class MetaFormUiState(
    val nombre: String = "",
    val montoTexto: String = "",
    val fechaLimite: Long = LocalDate.now().plusMonths(3).atTime(12, 0)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), // por defecto: dentro de 3 meses
    val icono: String = "alcancia",
    val errorNombre: String? = null,
    val errorMonto: String? = null,
    val errorFecha: String? = null,
    val esEdicion: Boolean = false,
    val guardando: Boolean = false,
    val terminado: Boolean = false
)

/** Crear o editar una meta de ahorro (mismo patrón que el formulario de movimientos). */
class MetaFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val metaRepository: MetaRepository,
    private val preferencias: PreferenciasRepository
) : ViewModel() {

    private val idMeta: Long = savedStateHandle.get<Long>(ARG_ID) ?: -1L
    private var original: MetaAhorro? = null

    var uiState by mutableStateOf(MetaFormUiState(esEdicion = idMeta > 0))
        private set

    init {
        if (idMeta > 0) {
            viewModelScope.launch {
                val usuarioId = preferencias.usuarioIdSesion.first() ?: return@launch
                metaRepository.obtener(idMeta, usuarioId)?.let { m ->
                    original = m
                    uiState = uiState.copy(
                        nombre = m.nombre,
                        montoTexto = if (m.montoObjetivo % 1.0 == 0.0) m.montoObjetivo.toLong().toString() else m.montoObjetivo.toString(),
                        fechaLimite = m.fechaLimite,
                        icono = m.icono
                    )
                }
            }
        }
    }

    fun onNombre(v: String) {
        uiState = uiState.copy(nombre = v.take(40), errorNombre = null)
    }

    fun onMonto(v: String) {
        uiState = uiState.copy(montoTexto = v.filter { it.isDigit() || it == '.' || it == ',' }.take(12), errorMonto = null)
    }

    fun onFecha(millis: Long) {
        uiState = uiState.copy(fechaLimite = millis, errorFecha = null)
    }

    fun onIcono(clave: String) {
        uiState = uiState.copy(icono = clave)
    }

    fun guardar() {
        if (uiState.guardando) return
        val s = uiState
        val errorNombre = if (s.nombre.isBlank()) "Ponle un nombre a tu meta" else null
        val errorMonto = Validaciones.validarMonto(s.montoTexto)
        // Al crear, la fecha límite no puede estar en el pasado (al editar se permite conservarla)
        val errorFecha = if (!s.esEdicion && Fechas.aLocalDate(s.fechaLimite) < LocalDate.now()) {
            "La fecha límite debe ser hoy o en el futuro"
        } else null
        if (errorNombre != null || errorMonto != null || errorFecha != null) {
            uiState = s.copy(errorNombre = errorNombre, errorMonto = errorMonto, errorFecha = errorFecha)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(guardando = true)
            val usuarioId = preferencias.usuarioIdSesion.first() ?: return@launch
            metaRepository.guardar(
                MetaAhorro(
                    id = original?.id ?: 0L,
                    usuarioId = usuarioId,
                    nombre = s.nombre.trim(),
                    montoObjetivo = Validaciones.parsearMonto(s.montoTexto)!!,
                    montoActual = original?.montoActual ?: 0.0, // editar no borra lo ya ahorrado
                    fechaLimite = s.fechaLimite,
                    icono = s.icono
                )
            )
            uiState = uiState.copy(guardando = false, terminado = true)
        }
    }

    companion object {
        const val ARG_ID = "id"
    }
}
