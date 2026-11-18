package com.example.mibolsillo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mibolsillo.data.local.entity.Presupuesto
import com.example.mibolsillo.data.local.model.PresupuestoConGasto
import kotlinx.coroutines.flow.Flow

/** Operaciones sobre la tabla "presupuestos". */
@Dao
interface PresupuestoDao {

    /**
     * Crea o actualiza. Como (usuario, categoría, mes, año) es único, REPLACE sustituye
     * el presupuesto anterior de esa categoría en ese mes si ya existía.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(presupuesto: Presupuesto): Long

    /** IGNORE: si ya existe un presupuesto para esa categoría y mes, lo deja como está. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarVarios(presupuestos: List<Presupuesto>)

    @Query("DELETE FROM presupuestos WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun eliminar(id: Long, usuarioId: Long)

    @Query("SELECT * FROM presupuestos WHERE usuarioId = :usuarioId AND mes = :mes AND anio = :anio")
    suspend fun delMes(usuarioId: Long, mes: Int, anio: Int): List<Presupuesto>

    /**
     * Presupuestos del mes con lo gastado en cada categoría.
     * La subconsulta (SELECT SUM...) suma los GASTOS de esa categoría dentro del mes.
     */
    @Query(
        """
        SELECT p.id, p.categoriaId, p.montoLimite, p.mes, p.anio,
               c.nombre, c.icono, c.color,
               COALESCE((
                   SELECT SUM(m.monto) FROM movimientos m
                   WHERE m.usuarioId = p.usuarioId
                     AND m.categoriaId = p.categoriaId
                     AND m.tipo = 'GASTO'
                     AND m.fecha BETWEEN :desde AND :hasta
               ), 0) AS gastado
        FROM presupuestos p
        INNER JOIN categorias c ON c.id = p.categoriaId
        WHERE p.usuarioId = :usuarioId AND p.mes = :mes AND p.anio = :anio
        ORDER BY c.nombre
        """
    )
    fun observarConGasto(
        usuarioId: Long,
        mes: Int,
        anio: Int,
        desde: Long,
        hasta: Long
    ): Flow<List<PresupuestoConGasto>>
}
