package com.example.mibolsillo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mibolsillo.data.local.entity.Movimiento
import com.example.mibolsillo.data.local.model.MovimientoConCategoria
import com.example.mibolsillo.data.local.model.TotalMensual
import com.example.mibolsillo.data.local.model.TotalPorCategoria
import kotlinx.coroutines.flow.Flow

/** Parte común de las consultas que devuelven un movimiento con su categoría (JOIN). */
private const val SELECT_CON_CATEGORIA = """
    SELECT m.*,
           c.id AS cat_id, c.nombre AS cat_nombre, c.icono AS cat_icono,
           c.color AS cat_color, c.tipo AS cat_tipo
    FROM movimientos m
    INNER JOIN categorias c ON c.id = m.categoriaId
"""

/**
 * Operaciones sobre la tabla "movimientos".
 * TODAS las consultas filtran por usuarioId: cada usuario solo ve sus propios datos.
 * Las que devuelven Flow se vuelven a ejecutar solas cuando la tabla cambia.
 */
@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: Movimiento): Long

    /** Inserta muchos de una vez (en una sola transacción). Lo usan los datos de demostración. */
    @Insert
    suspend fun insertarVarios(movimientos: List<Movimiento>)

    @Update
    suspend fun actualizar(movimiento: Movimiento)

    @Delete
    suspend fun eliminar(movimiento: Movimiento)

    @Query("SELECT COUNT(*) FROM movimientos WHERE usuarioId = :usuarioId")
    suspend fun contar(usuarioId: Long): Int

    @Query("SELECT * FROM movimientos WHERE id = :id AND usuarioId = :usuarioId LIMIT 1")
    suspend fun obtener(id: Long, usuarioId: Long): Movimiento?

    /**
     * Lista filtrada para la pantalla Movimientos.
     * Truco SQL: "(:tipo IS NULL OR m.tipo = :tipo)" hace que el filtro sea opcional:
     * si se pasa null, la condición siempre es verdadera.
     */
    @Query(
        SELECT_CON_CATEGORIA + """
        WHERE m.usuarioId = :usuarioId
          AND m.fecha BETWEEN :desde AND :hasta
          AND (:tipo IS NULL OR m.tipo = :tipo)
          AND (:categoriaId IS NULL OR m.categoriaId = :categoriaId)
          AND (:texto = '' OR m.nota LIKE '%' || :texto || '%' OR c.nombre LIKE '%' || :texto || '%')
        ORDER BY m.fecha DESC, m.id DESC
        """
    )
    fun filtrar(
        usuarioId: Long,
        desde: Long,
        hasta: Long,
        tipo: String?,
        categoriaId: Long?,
        texto: String
    ): Flow<List<MovimientoConCategoria>>

    /** Los últimos [limite] movimientos (para Inicio). */
    @Query(SELECT_CON_CATEGORIA + " WHERE m.usuarioId = :usuarioId ORDER BY m.fecha DESC, m.id DESC LIMIT :limite")
    fun ultimos(usuarioId: Long, limite: Int): Flow<List<MovimientoConCategoria>>

    /** Saldo total = suma de ingresos - suma de gastos (de toda la historia). */
    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE -monto END), 0)
        FROM movimientos WHERE usuarioId = :usuarioId
        """
    )
    fun saldo(usuarioId: Long): Flow<Double>

    /** Total de un tipo (INGRESO o GASTO) en un rango de fechas, p. ej. el mes actual. */
    @Query(
        """
        SELECT COALESCE(SUM(monto), 0) FROM movimientos
        WHERE usuarioId = :usuarioId AND tipo = :tipo AND fecha BETWEEN :desde AND :hasta
        """
    )
    fun total(usuarioId: Long, tipo: String, desde: Long, hasta: Long): Flow<Double>

    /** Totales agrupados por categoría en un rango (GROUP BY), de mayor a menor. */
    @Query(
        """
        SELECT c.id AS categoriaId, c.nombre AS nombre, c.icono AS icono, c.color AS color,
               SUM(m.monto) AS total
        FROM movimientos m
        INNER JOIN categorias c ON c.id = m.categoriaId
        WHERE m.usuarioId = :usuarioId AND m.tipo = :tipo AND m.fecha BETWEEN :desde AND :hasta
        GROUP BY c.id
        ORDER BY total DESC
        """
    )
    fun totalesPorCategoria(usuarioId: Long, tipo: String, desde: Long, hasta: Long): Flow<List<TotalPorCategoria>>

    /**
     * Totales por MES y TIPO desde una fecha (para la gráfica de barras de 6 meses).
     * strftime convierte los milisegundos en texto "2026-11" usando la hora local del teléfono.
     */
    @Query(
        """
        SELECT strftime('%Y-%m', fecha / 1000, 'unixepoch', 'localtime') AS mes,
               tipo,
               SUM(monto) AS total
        FROM movimientos
        WHERE usuarioId = :usuarioId AND fecha BETWEEN :desde AND :hasta
        GROUP BY mes, tipo
        """
    )
    fun totalesMensuales(usuarioId: Long, desde: Long, hasta: Long): Flow<List<TotalMensual>>
}
