package com.example.mibolsillo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mibolsillo.data.local.entity.Usuario
import kotlinx.coroutines.flow.Flow

/** Operaciones de base de datos sobre la tabla "usuarios". */
@Dao
interface UsuarioDao {

    /** Inserta un usuario y devuelve su nuevo id. Falla si el correo ya existe (índice único). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Usuario?

    /** Versión reactiva (Flow): la UI se actualiza sola si cambian los datos del usuario. */
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun observarPorId(id: Long): Flow<Usuario?>
}
