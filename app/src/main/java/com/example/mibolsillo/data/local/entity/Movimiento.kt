package com.example.mibolsillo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "movimientos": cada ingreso o gasto registrado por un usuario.
 *
 * Relaciones (claves foráneas):
 *  - usuarioId   -> usuarios.id   (si se borra el usuario, se borran sus movimientos: CASCADE)
 *  - categoriaId -> categorias.id (no se puede borrar una categoría en uso: RESTRICT)
 *
 * @param fecha fecha del movimiento en milisegundos (epoch). Facilita ordenar y filtrar por mes.
 */
@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("usuarioId"), Index("categoriaId"), Index("fecha")]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val categoriaId: Long,
    val monto: Double,
    val tipo: TipoMovimiento,
    val fecha: Long,
    val nota: String = ""
)
