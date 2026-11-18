package com.example.mibolsillo.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mibolsillo.data.local.entity.Categoria
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import kotlinx.coroutines.flow.Flow

/** Operaciones sobre la tabla "categorias" (solo lectura: se precargan al crear la BD). */
@Dao
interface CategoriaDao {

    @Query("SELECT * FROM categorias ORDER BY tipo DESC, nombre")
    fun observarTodas(): Flow<List<Categoria>>

    /** Versión de una sola lectura (no reactiva). */
    @Query("SELECT * FROM categorias")
    suspend fun obtenerTodas(): List<Categoria>

    @Query("SELECT * FROM categorias WHERE tipo = :tipo ORDER BY nombre")
    fun observarPorTipo(tipo: TipoMovimiento): Flow<List<Categoria>>
}
