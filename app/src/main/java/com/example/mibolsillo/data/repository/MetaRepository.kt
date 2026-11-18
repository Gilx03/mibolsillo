package com.example.mibolsillo.data.repository

import com.example.mibolsillo.data.local.dao.MetaAhorroDao
import com.example.mibolsillo.data.local.entity.MetaAhorro
import kotlinx.coroutines.flow.Flow

/** Repositorio de metas de ahorro. */
class MetaRepository(private val dao: MetaAhorroDao) {

    fun observar(usuarioId: Long): Flow<List<MetaAhorro>> = dao.observar(usuarioId)

    suspend fun obtener(id: Long, usuarioId: Long): MetaAhorro? = dao.obtener(id, usuarioId)

    suspend fun guardar(meta: MetaAhorro) {
        if (meta.id == 0L) dao.insertar(meta) else dao.actualizar(meta)
    }

    suspend fun eliminar(id: Long, usuarioId: Long) = dao.eliminar(id, usuarioId)

    /** Registra un aporte y devuelve la meta ya actualizada (para saber si se completó). */
    suspend fun aportar(id: Long, usuarioId: Long, monto: Double): MetaAhorro? {
        dao.aportar(id, usuarioId, monto)
        return dao.obtener(id, usuarioId)
    }
}
