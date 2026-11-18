package com.example.mibolsillo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "metas_ahorro": objetivo de ahorro de un usuario (p. ej. "Laptop nueva").
 * montoActual se incrementa cada vez que el usuario registra un aporte.
 *
 * @param fechaLimite fecha objetivo en milisegundos (epoch).
 * @param icono clave de texto del icono (ver ui/components/IconosApp.kt).
 */
@Entity(
    tableName = "metas_ahorro",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class MetaAhorro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val nombre: String,
    val montoObjetivo: Double,
    val montoActual: Double = 0.0,
    val fechaLimite: Long,
    val icono: String
) {
    /** Progreso entre 0.0 y 1.0, útil para las barras de progreso. */
    val progreso: Float
        get() = if (montoObjetivo <= 0) 0f else (montoActual / montoObjetivo).coerceIn(0.0, 1.0).toFloat()

    /** true cuando ya se alcanzó (o superó) el monto objetivo. */
    val completada: Boolean
        get() = montoActual >= montoObjetivo
}
