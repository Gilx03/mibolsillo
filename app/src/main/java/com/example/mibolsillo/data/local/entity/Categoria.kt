package com.example.mibolsillo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla "categorias". Son compartidas por todos los usuarios y se precargan
 * al crear la base de datos (ver CategoriasIniciales en AppDatabase.kt).
 *
 * @param icono clave de texto que la UI traduce a un ImageVector (ver ui/components/IconosApp.kt).
 * @param color color ARGB en formato Long, p. ej. 0xFFFF7043.
 * @param tipo si la categoría es de INGRESO o de GASTO.
 */
@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val icono: String,
    val color: Long,
    val tipo: TipoMovimiento
)
