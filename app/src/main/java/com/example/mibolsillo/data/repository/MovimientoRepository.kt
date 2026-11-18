package com.example.mibolsillo.data.repository

import com.example.mibolsillo.data.local.dao.MovimientoDao
import com.example.mibolsillo.data.local.entity.Movimiento
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.data.local.model.MovimientoConCategoria
import com.example.mibolsillo.data.local.model.TotalMensual
import com.example.mibolsillo.data.local.model.TotalPorCategoria
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de movimientos: punto único de acceso a los ingresos y gastos.
 * Traduce los tipos de la app (enum TipoMovimiento) a lo que espera el DAO (texto).
 */
class MovimientoRepository(private val dao: MovimientoDao) {

    suspend fun guardar(movimiento: Movimiento) {
        if (movimiento.id == 0L) dao.insertar(movimiento) else dao.actualizar(movimiento)
    }

    suspend fun eliminar(movimiento: Movimiento) = dao.eliminar(movimiento)

    suspend fun obtener(id: Long, usuarioId: Long): Movimiento? = dao.obtener(id, usuarioId)

    fun filtrar(
        usuarioId: Long,
        desde: Long,
        hasta: Long,
        tipo: TipoMovimiento?,
        categoriaId: Long?,
        texto: String
    ): Flow<List<MovimientoConCategoria>> =
        dao.filtrar(usuarioId, desde, hasta, tipo?.name, categoriaId, texto.trim())

    fun ultimos(usuarioId: Long, limite: Int = 5) = dao.ultimos(usuarioId, limite)

    fun saldo(usuarioId: Long): Flow<Double> = dao.saldo(usuarioId)

    fun total(usuarioId: Long, tipo: TipoMovimiento, desde: Long, hasta: Long): Flow<Double> =
        dao.total(usuarioId, tipo.name, desde, hasta)

    fun totalesPorCategoria(
        usuarioId: Long,
        tipo: TipoMovimiento,
        desde: Long,
        hasta: Long
    ): Flow<List<TotalPorCategoria>> = dao.totalesPorCategoria(usuarioId, tipo.name, desde, hasta)

    fun totalesMensuales(usuarioId: Long, desde: Long, hasta: Long): Flow<List<TotalMensual>> =
        dao.totalesMensuales(usuarioId, desde, hasta)
}
