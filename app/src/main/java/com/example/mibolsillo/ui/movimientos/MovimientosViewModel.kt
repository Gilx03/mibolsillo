package com.example.mibolsillo.ui.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mibolsillo.data.local.entity.Categoria
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.data.local.model.MovimientoConCategoria
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import com.example.mibolsillo.data.repository.CategoriaRepository
import com.example.mibolsillo.data.repository.MovimientoRepository
import com.example.mibolsillo.util.Fechas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

/** Filtros elegidos por el usuario. null = "sin filtro" (todos). */
data class FiltrosMovimientos(
    val mes: YearMonth = YearMonth.now(),
    val tipo: TipoMovimiento? = null,
    val categoriaId: Long? = null
)

/** Movimientos de un mismo día (para mostrar encabezados "Hoy", "Ayer", etc.). */
data class GrupoDia(val fecha: LocalDate, val movimientos: List<MovimientoConCategoria>)

data class MovimientosUiState(
    val filtros: FiltrosMovimientos = FiltrosMovimientos(),
    val grupos: List<GrupoDia> = emptyList(),
    val totalIngresos: Double = 0.0,
    val totalGastos: Double = 0.0,
    val categorias: List<Categoria> = emptyList(),
    val cargando: Boolean = true
) {
    val vacio: Boolean get() = grupos.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MovimientosViewModel(
    movimientoRepository: MovimientoRepository,
    categoriaRepository: CategoriaRepository,
    preferencias: PreferenciasRepository
) : ViewModel() {

    private val filtros = MutableStateFlow(FiltrosMovimientos())

    // El texto de búsqueda va aparte para que el campo responda al instante,
    // pero la consulta solo se lanza 300 ms después de que el usuario deja de escribir (debounce).
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    /** Cada vez que cambia el usuario, un filtro o la búsqueda, se relanza la consulta a Room. */
    private val movimientos = combine(
        preferencias.usuarioIdSesion.filterNotNull(),
        filtros,
        _busqueda.debounce(300)
    ) { usuarioId, f, texto -> Triple(usuarioId, f, texto) }
        .flatMapLatest { (usuarioId, f, texto) ->
            movimientoRepository.filtrar(
                usuarioId = usuarioId,
                desde = Fechas.inicioMes(f.mes),
                hasta = Fechas.finMes(f.mes),
                tipo = f.tipo,
                categoriaId = f.categoriaId,
                texto = texto
            )
        }

    val uiState: StateFlow<MovimientosUiState> = combine(
        movimientos, filtros, categoriaRepository.todas
    ) { lista, f, categorias ->
        MovimientosUiState(
            filtros = f,
            // groupBy conserva el orden (ya vienen ordenados por fecha descendente)
            grupos = lista.groupBy { Fechas.aLocalDate(it.movimiento.fecha) }
                .map { (fecha, items) -> GrupoDia(fecha, items) },
            totalIngresos = lista.filter { it.movimiento.tipo == TipoMovimiento.INGRESO }.sumOf { it.movimiento.monto },
            totalGastos = lista.filter { it.movimiento.tipo == TipoMovimiento.GASTO }.sumOf { it.movimiento.monto },
            categorias = categorias,
            cargando = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MovimientosUiState())

    fun buscar(texto: String) {
        _busqueda.value = texto
    }

    fun cambiarTipo(tipo: TipoMovimiento?) = filtros.update { f ->
        // Si la categoría elegida no es del nuevo tipo, se quita el filtro de categoría
        val categoria = uiState.value.categorias.firstOrNull { it.id == f.categoriaId }
        val mantenerCategoria = tipo == null || categoria?.tipo == tipo
        f.copy(tipo = tipo, categoriaId = if (mantenerCategoria) f.categoriaId else null)
    }

    fun cambiarCategoria(categoriaId: Long?) = filtros.update { it.copy(categoriaId = categoriaId) }

    fun mesAnterior() = filtros.update { it.copy(mes = it.mes.minusMonths(1)) }

    fun mesSiguiente() = filtros.update { f ->
        if (f.mes < YearMonth.now()) f.copy(mes = f.mes.plusMonths(1)) else f // no avanzar al futuro
    }

    fun limpiarFiltros() {
        filtros.update { FiltrosMovimientos(mes = it.mes) }
        _busqueda.value = ""
    }
}
