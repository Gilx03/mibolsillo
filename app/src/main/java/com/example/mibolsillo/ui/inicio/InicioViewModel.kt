package com.example.mibolsillo.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mibolsillo.data.consejos.Consejo
import com.example.mibolsillo.data.consejos.Consejos
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.data.local.model.EstadoPresupuesto
import com.example.mibolsillo.data.local.model.MovimientoConCategoria
import com.example.mibolsillo.data.local.model.PresupuestoConGasto
import com.example.mibolsillo.data.local.model.TotalPorCategoria
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import com.example.mibolsillo.data.repository.MovimientoRepository
import com.example.mibolsillo.data.repository.PresupuestoRepository
import com.example.mibolsillo.data.repository.UsuarioRepository
import com.example.mibolsillo.util.Fechas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class InicioUiState(
    val nombre: String = "",
    val mes: YearMonth = YearMonth.now(),
    val saldo: Double = 0.0,
    val ingresosMes: Double = 0.0,
    val gastosMes: Double = 0.0,
    val gastosPorCategoria: List<TotalPorCategoria> = emptyList(),
    val ultimos: List<MovimientoConCategoria> = emptyList(),
    /** Presupuestos del mes que llegaron al 80% o más (se muestran como alertas). */
    val alertas: List<PresupuestoConGasto> = emptyList(),
    val cargando: Boolean = true
)

/**
 * ViewModel del dashboard. Junta (combine) varias consultas reactivas de Room
 * en un solo estado: si se agrega un movimiento, TODO el Inicio se actualiza solo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InicioViewModel(
    usuarioRepository: UsuarioRepository,
    movimientoRepository: MovimientoRepository,
    presupuestoRepository: PresupuestoRepository,
    preferencias: PreferenciasRepository
) : ViewModel() {

    /** Mismo consejo durante todo el día. */
    val consejoDelDia: Consejo = Consejos.delDia()

    val uiState: StateFlow<InicioUiState> = preferencias.usuarioIdSesion
        .filterNotNull()
        .flatMapLatest { usuarioId ->
            val mes = YearMonth.now()
            val desde = Fechas.inicioMes(mes)
            val hasta = Fechas.finMes(mes)
            combine(
                movimientoRepository.saldo(usuarioId),
                movimientoRepository.total(usuarioId, TipoMovimiento.INGRESO, desde, hasta),
                movimientoRepository.total(usuarioId, TipoMovimiento.GASTO, desde, hasta),
                movimientoRepository.totalesPorCategoria(usuarioId, TipoMovimiento.GASTO, desde, hasta),
                movimientoRepository.ultimos(usuarioId, 5)
            ) { saldo, ingresos, gastos, porCategoria, ultimos ->
                InicioUiState(
                    mes = mes,
                    saldo = saldo,
                    ingresosMes = ingresos,
                    gastosMes = gastos,
                    gastosPorCategoria = porCategoria,
                    ultimos = ultimos,
                    cargando = false
                )
            }
                // combine acepta hasta 5 flujos con tipos distintos: el sexto se añade aparte
                .combine(presupuestoRepository.observarConGasto(usuarioId, mes)) { estado, presupuestos ->
                    estado.copy(
                        alertas = presupuestos
                            .filter { it.estado != EstadoPresupuesto.NORMAL }
                            .sortedByDescending { it.porcentaje }
                    )
                }
        }
        // El nombre viene de otra tabla: lo añadimos combinando con el usuario actual
        .combine(usuarioRepository.usuarioActual) { estado, usuario ->
            estado.copy(nombre = usuario?.nombre.orEmpty())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioUiState())
}
