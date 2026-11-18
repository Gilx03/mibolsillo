package com.example.mibolsillo.ui.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.data.local.model.TotalPorCategoria
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import com.example.mibolsillo.data.repository.MovimientoRepository
import com.example.mibolsillo.util.Fechas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth

/** Ingresos y gastos de un mes (una columna de la gráfica de barras). */
data class BarraMes(val mes: YearMonth, val ingresos: Double, val gastos: Double)

data class EstadisticasUiState(
    val mes: YearMonth = YearMonth.now(),
    val tipo: TipoMovimiento = TipoMovimiento.GASTO,
    val porCategoria: List<TotalPorCategoria> = emptyList(),
    val ingresosMes: Double = 0.0,
    val gastosMes: Double = 0.0,
    val barras: List<BarraMes> = emptyList(),
    val barraSeleccionada: Int = MESES_GRAFICA - 1,
    val cargando: Boolean = true
) {
    /** Total del tipo que se ve en el pastel (gastos o ingresos del mes). */
    val totalPastel: Double get() = if (tipo == TipoMovimiento.GASTO) gastosMes else ingresosMes

    /** % de los ingresos que NO se gastó. null si no hubo ingresos (no se puede calcular). */
    val tasaAhorro: Double? get() = if (ingresosMes > 0) (ingresosMes - gastosMes) / ingresosMes else null

    /** Promedio de gasto mensual de los 6 meses de la gráfica. */
    val promedioGastos: Double get() = if (barras.isEmpty()) 0.0 else barras.sumOf { it.gastos } / barras.size

    /** Variación del gasto respecto al mes anterior (0.25 = +25%). null si el mes anterior fue 0. */
    val variacionGastos: Double?
        get() {
            if (barras.size < 2) return null
            val anterior = barras[barras.size - 2].gastos
            return if (anterior > 0) (barras.last().gastos - anterior) / anterior else null
        }

    /** true si no hay ningún movimiento en los 6 meses: se muestra el estado vacío. */
    val sinDatos: Boolean get() = barras.all { it.ingresos == 0.0 && it.gastos == 0.0 }
}

const val MESES_GRAFICA = 6

private data class FiltrosEstadisticas(val mes: YearMonth, val tipo: TipoMovimiento)

@OptIn(ExperimentalCoroutinesApi::class)
class EstadisticasViewModel(
    movimientoRepository: MovimientoRepository,
    preferencias: PreferenciasRepository
) : ViewModel() {

    private val filtros = MutableStateFlow(FiltrosEstadisticas(YearMonth.now(), TipoMovimiento.GASTO))
    private val barraSeleccionada = MutableStateFlow(MESES_GRAFICA - 1)

    private val datos = combine(preferencias.usuarioIdSesion.filterNotNull(), filtros) { id, f -> id to f }
        .flatMapLatest { (usuarioId, f) ->
            val desde = Fechas.inicioMes(f.mes)
            val hasta = Fechas.finMes(f.mes)
            // La gráfica de barras muestra los 6 meses que terminan en el mes elegido
            val primerMes = f.mes.minusMonths((MESES_GRAFICA - 1).toLong())

            combine(
                movimientoRepository.totalesPorCategoria(usuarioId, f.tipo, desde, hasta),
                movimientoRepository.total(usuarioId, TipoMovimiento.INGRESO, desde, hasta),
                movimientoRepository.total(usuarioId, TipoMovimiento.GASTO, desde, hasta),
                movimientoRepository.totalesMensuales(usuarioId, Fechas.inicioMes(primerMes), hasta)
            ) { porCategoria, ingresos, gastos, mensuales ->
                // Room solo devuelve los meses que tienen datos; rellenamos los vacíos con 0
                val barras = (0 until MESES_GRAFICA).map { i ->
                    val mes = primerMes.plusMonths(i.toLong())
                    val clave = mes.toString() // YearMonth.toString() = "2026-11", igual que strftime
                    BarraMes(
                        mes = mes,
                        ingresos = mensuales.firstOrNull { it.mes == clave && it.tipo == TipoMovimiento.INGRESO.name }?.total ?: 0.0,
                        gastos = mensuales.firstOrNull { it.mes == clave && it.tipo == TipoMovimiento.GASTO.name }?.total ?: 0.0
                    )
                }
                EstadisticasUiState(
                    mes = f.mes,
                    tipo = f.tipo,
                    porCategoria = porCategoria,
                    ingresosMes = ingresos,
                    gastosMes = gastos,
                    barras = barras,
                    cargando = false
                )
            }
        }

    val uiState: StateFlow<EstadisticasUiState> = combine(datos, barraSeleccionada) { estado, seleccion ->
        estado.copy(barraSeleccionada = seleccion)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadisticasUiState())

    fun cambiarTipo(tipo: TipoMovimiento) = filtros.update { it.copy(tipo = tipo) }

    fun mesAnterior() {
        filtros.update { it.copy(mes = it.mes.minusMonths(1)) }
        barraSeleccionada.value = MESES_GRAFICA - 1
    }

    fun mesSiguiente() {
        if (filtros.value.mes >= YearMonth.now()) return
        filtros.update { it.copy(mes = it.mes.plusMonths(1)) }
        barraSeleccionada.value = MESES_GRAFICA - 1
    }

    fun seleccionarBarra(indice: Int) {
        barraSeleccionada.value = indice
    }
}
