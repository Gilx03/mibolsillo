package com.example.mibolsillo.ui.presupuestos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mibolsillo.data.local.entity.Categoria
import com.example.mibolsillo.data.local.entity.Presupuesto
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.data.local.model.PresupuestoConGasto
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import com.example.mibolsillo.data.repository.CategoriaRepository
import com.example.mibolsillo.data.repository.PresupuestoRepository
import com.example.mibolsillo.util.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class PresupuestosUiState(
    val mes: YearMonth = YearMonth.now(),
    val presupuestos: List<PresupuestoConGasto> = emptyList(),
    val categoriasGasto: List<Categoria> = emptyList(),
    val cargando: Boolean = true
) {
    val totalLimite: Double get() = presupuestos.sumOf { it.montoLimite }
    val totalGastado: Double get() = presupuestos.sumOf { it.gastado }

    /** Categorías de gasto que todavía no tienen presupuesto este mes (para el diálogo "Nuevo"). */
    val categoriasDisponibles: List<Categoria>
        get() = categoriasGasto.filter { c -> presupuestos.none { it.categoriaId == c.id } }
}

/** Estado del diálogo para crear/editar un presupuesto. null = diálogo cerrado. */
data class FormPresupuesto(
    val id: Long = 0,
    val categoriaId: Long? = null,
    val nombreCategoria: String = "",
    val montoTexto: String = "",
    val errorCategoria: String? = null,
    val errorMonto: String? = null
) {
    val esEdicion: Boolean get() = id != 0L
}

@OptIn(ExperimentalCoroutinesApi::class)
class PresupuestosViewModel(
    private val presupuestoRepository: PresupuestoRepository,
    categoriaRepository: CategoriaRepository,
    private val preferencias: PreferenciasRepository
) : ViewModel() {

    private val mes = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<PresupuestosUiState> = combine(
        preferencias.usuarioIdSesion.filterNotNull(),
        mes
    ) { id, m -> id to m }
        .flatMapLatest { (usuarioId, m) ->
            combine(
                presupuestoRepository.observarConGasto(usuarioId, m),
                categoriaRepository.porTipo(TipoMovimiento.GASTO)
            ) { lista, categorias -> PresupuestosUiState(m, lista, categorias, cargando = false) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PresupuestosUiState())

    /** Diálogo abierto (o null). Lo maneja el ViewModel para que sobreviva a rotaciones. */
    var form by mutableStateOf<FormPresupuesto?>(null)
        private set

    /** Mensaje corto para mostrar en un Snackbar (luego se limpia). */
    var mensaje by mutableStateOf<String?>(null)
        private set

    // ---------------- Navegación de meses ----------------
    fun mesAnterior() {
        mes.value = mes.value.minusMonths(1)
    }

    fun mesSiguiente() {
        if (mes.value < YearMonth.now().plusMonths(1)) mes.value = mes.value.plusMonths(1)
    }

    // ---------------- Diálogo ----------------
    fun abrirNuevo() {
        form = FormPresupuesto()
    }

    fun abrirEditar(p: PresupuestoConGasto) {
        form = FormPresupuesto(
            id = p.id,
            categoriaId = p.categoriaId,
            nombreCategoria = p.nombre,
            montoTexto = if (p.montoLimite % 1.0 == 0.0) p.montoLimite.toLong().toString() else p.montoLimite.toString()
        )
    }

    fun cerrarForm() {
        form = null
    }

    fun onCategoria(id: Long) {
        form = form?.copy(categoriaId = id, errorCategoria = null)
    }

    fun onMonto(texto: String) {
        form = form?.copy(
            montoTexto = texto.filter { it.isDigit() || it == '.' || it == ',' }.take(12),
            errorMonto = null
        )
    }

    fun guardarForm() {
        val f = form ?: return
        val errorMonto = Validaciones.validarMonto(f.montoTexto)
        val errorCategoria = if (f.categoriaId == null) "Elige una categoría" else null
        if (errorMonto != null || errorCategoria != null) {
            form = f.copy(errorMonto = errorMonto, errorCategoria = errorCategoria)
            return
        }
        viewModelScope.launch {
            val usuarioId = preferencias.usuarioIdSesion.first() ?: return@launch
            val m = mes.value
            presupuestoRepository.guardar(
                Presupuesto(
                    id = f.id,
                    usuarioId = usuarioId,
                    categoriaId = f.categoriaId!!,
                    montoLimite = Validaciones.parsearMonto(f.montoTexto)!!,
                    mes = m.monthValue,
                    anio = m.year
                )
            )
            form = null
            mensaje = if (f.esEdicion) "Presupuesto actualizado" else "Presupuesto creado"
        }
    }

    fun eliminarDesdeForm() {
        val f = form ?: return
        viewModelScope.launch {
            val usuarioId = preferencias.usuarioIdSesion.first() ?: return@launch
            presupuestoRepository.eliminar(f.id, usuarioId)
            form = null
            mensaje = "Presupuesto de ${f.nombreCategoria} eliminado"
        }
    }

    fun copiarMesAnterior() {
        viewModelScope.launch {
            val usuarioId = preferencias.usuarioIdSesion.first() ?: return@launch
            val copiados = presupuestoRepository.copiarDelMesAnterior(usuarioId, mes.value)
            mensaje = if (copiados == 0) "El mes anterior no tenía presupuestos" else "Se copiaron $copiados presupuestos"
        }
    }

    fun mensajeMostrado() {
        mensaje = null
    }
}
