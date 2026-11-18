package com.example.mibolsillo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "presupuestos": límite de gasto mensual de un usuario para una categoría.
 * El índice único (usuarioId, categoriaId, mes, anio) evita dos presupuestos
 * para la misma categoría en el mismo mes.
 *
 * @param mes 1 = enero ... 12 = diciembre.
 */
@Entity(
    tableName = "presupuestos",
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
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["usuarioId", "categoriaId", "mes", "anio"], unique = true),
        Index("categoriaId")
    ]
)
data class Presupuesto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val categoriaId: Long,
    val montoLimite: Double,
    val mes: Int,
    val anio: Int
)
