package com.example.mibolsillo.data.repository

import com.example.mibolsillo.data.local.dao.CategoriaDao
import com.example.mibolsillo.data.local.entity.Categoria
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import kotlinx.coroutines.flow.Flow

/** Repositorio de categorías (solo lectura). */
class CategoriaRepository(private val dao: CategoriaDao) {

    val todas: Flow<List<Categoria>> = dao.observarTodas()

    fun porTipo(tipo: TipoMovimiento): Flow<List<Categoria>> = dao.observarPorTipo(tipo)
}
