package com.example.mibolsillo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mibolsillo.data.local.entity.MetaAhorro
import kotlinx.coroutines.flow.Flow

/** Operaciones sobre la tabla "metas_ahorro". */
@Dao
interface MetaAhorroDao {

    @Insert
    suspend fun insertar(meta: MetaAhorro): Long

    @Update
    suspend fun actualizar(meta: MetaAhorro)

    @Query("DELETE FROM metas_ahorro WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun eliminar(id: Long, usuarioId: Long)

    @Query("SELECT * FROM metas_ahorro WHERE id = :id AND usuarioId = :usuarioId LIMIT 1")
    suspend fun obtener(id: Long, usuarioId: Long): MetaAhorro?

    /** Primero las metas en curso (ordenadas por fecha límite) y al final las completadas. */
    @Query(
        """
        SELECT * FROM metas_ahorro WHERE usuarioId = :usuarioId
        ORDER BY (montoActual >= montoObjetivo), fechaLimite
        """
    )
    fun observar(usuarioId: Long): Flow<List<MetaAhorro>>

    /** Suma un aporte directamente en SQL (sin tener que leer y volver a escribir la meta). */
    @Query("UPDATE metas_ahorro SET montoActual = montoActual + :monto WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun aportar(id: Long, usuarioId: Long, monto: Double)
}
