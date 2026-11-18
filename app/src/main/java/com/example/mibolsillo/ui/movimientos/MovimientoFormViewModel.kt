package com.example.mibolsillo.ui.movimientos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mibolsillo.data.local.entity.Categoria
import com.example.mibolsillo.data.local.entity.Movimiento
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import com.example.mibolsillo.data.repository.CategoriaRepository
import com.example.mibolsillo.data.repository.MovimientoRepository
import com.example.mibolsillo.util.Validaciones
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MovimientoFormUiState(
    val tipo: TipoMovimiento = TipoMovimiento.GASTO,
    val montoTexto: String = "",
    val categoriaId: Long? = null,
    val fecha: Long = System.currentTimeMillis(),
    val nota: String = "",
    val errorMonto: String? = null,
    val errorCategoria: String? = null,
    val esEdicion: Boolean = false,
    val guardando: Boolean = false,
    val terminado: Boolean = false // true -> la pantalla debe cerrarse
)

/**
 * ViewModel del formulario de movimiento. Sirve para CREAR y para EDITAR:
 * si la ruta trae un id (> 0) carga ese movimiento; si no, empieza vacío.
 * SavedStateHandle contiene los argumentos de navegación (el "id").
 */
class MovimientoFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val movimientoRepository: MovimientoRepository,
    categoriaRepository: CategoriaRepository,
    private val preferencias: PreferenciasRepository
) : ViewModel() {

    private val idMovimiento: Long = savedStateHandle.get<Long>(ARG_ID) ?: -1L

    /** Movimiento original (solo al editar). Lo necesitamos para actualizar/eliminar. */
    private var original: Movimiento? = null

    var uiState by mutableStateOf(MovimientoFormUiState(esEdicion = idMovimiento > 0))
        private set

    val categorias: StateFlow<List<Categoria>> = categoriaRepository.todas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (idMovimiento > 0) {
            viewModelScope.launch {
                val usuarioId = preferencias.usuarioIdSesion.first() ?: return@launch
                movimientoRepository.obtener(idMovimiento, usuarioId)?.let { m ->
                    original = m
                    uiState = uiState.copy(
                        tipo = m.tipo,
                        montoTexto = formatearParaEditar(m.monto),
                        categoriaId = m.categoriaId,
                        fecha = m.fecha,
                        nota = m.nota
                    )
                }
            }
        }
    }

    fun onTipoChange(tipo: TipoMovimiento) {
        if (tipo == uiState.tipo) return
        // Las categorías son distintas para ingresos y gastos: al cambiar de tipo se reinicia la elección
        uiState = uiState.copy(tipo = tipo, categoriaId = null, errorCategoria = null)
    }

    fun onMontoChange(texto: String) {
        // Solo dígitos y un separador decimal; máximo 12 caracteres
        val filtrado = texto.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
        uiState = uiState.copy(montoTexto = filtrado, errorMonto = null)
    }

    fun onCategoriaChange(id: Long) {
        uiState = uiState.copy(categoriaId = id, errorCategoria = null)
    }

    fun onFechaChange(millis: Long) {
        uiState = uiState.copy(fecha = millis)
    }

    fun onNotaChange(texto: String) {
        uiState = uiState.copy(nota = texto.take(MAX_NOTA))
    }

    fun guardar() {
        if (uiState.guardando) return
        val errorMonto = Validaciones.validarMonto(uiState.montoTexto)
        val errorCategoria = if (uiState.categoriaId == null) "Elige una categoría" else null
        if (errorMonto != null || errorCategoria != null) {
            uiState = uiState.copy(errorMonto = errorMonto, errorCategoria = errorCategoria)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(guardando = true)
            val usuarioId = preferencias.usuarioIdSesion.first() ?: return@launch
            val movimiento = Movimiento(
                id = original?.id ?: 0L, // id 0 = nuevo (Room genera uno)
                usuarioId = usuarioId,
                categoriaId = uiState.categoriaId!!,
                monto = Validaciones.parsearMonto(uiState.montoTexto)!!,
                tipo = uiState.tipo,
                fecha = uiState.fecha,
                nota = uiState.nota.trim()
            )
            movimientoRepository.guardar(movimiento)
            uiState = uiState.copy(guardando = false, terminado = true)
        }
    }

    fun eliminar() {
        val m = original ?: return
        viewModelScope.launch {
            movimientoRepository.eliminar(m)
            uiState = uiState.copy(terminado = true)
        }
    }

    /** 1500.0 -> "1500" ; 1500.5 -> "1500.5" (sin separador de miles, para poder editarlo). */
    private fun formatearParaEditar(monto: Double): String =
        if (monto % 1.0 == 0.0) monto.toLong().toString() else monto.toString()

    companion object {
        const val ARG_ID = "id"
        const val MAX_NOTA = 100
    }
}
