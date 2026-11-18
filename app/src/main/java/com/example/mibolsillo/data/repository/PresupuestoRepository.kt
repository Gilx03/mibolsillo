package com.example.mibolsillo.data.repository

import com.example.mibolsillo.data.local.dao.PresupuestoDao
import com.example.mibolsillo.data.local.entity.Presupuesto
import com.example.mibolsillo.data.local.model.PresupuestoConGasto
import com.example.mibolsillo.util.Fechas
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/** Repositorio de presupuestos mensuales por categoría. */
class PresupuestoRepository(private val dao: PresupuestoDao) {

    /** Presupuestos de [mes] con lo gastado en cada categoría (se actualiza solo). */
    fun observarConGasto(usuarioId: Long, mes: YearMonth): Flow<List<PresupuestoConGasto>> =
        dao.observarConGasto(
            usuarioId = usuarioId,
            mes = mes.monthValue,
            anio = mes.year,
            desde = Fechas.inicioMes(mes),
            hasta = Fechas.finMes(mes)
        )

    suspend fun guardar(presupuesto: Presupuesto) = dao.guardar(presupuesto)

    suspend fun eliminar(id: Long, usuarioId: Long) = dao.eliminar(id, usuarioId)

    /**
     * Copia los límites del mes anterior al mes indicado (sin pisar los que ya existan).
     * @return cuántos presupuestos tenía el mes anterior (0 = no había nada que copiar).
     */
    suspend fun copiarDelMesAnterior(usuarioId: Long, mes: YearMonth): Int {
        val anterior = mes.minusMonths(1)
        val lista = dao.delMes(usuarioId, anterior.monthValue, anterior.year)
        dao.insertarVarios(lista.map { it.copy(id = 0, mes = mes.monthValue, anio = mes.year) })
        return lista.size
    }
}
