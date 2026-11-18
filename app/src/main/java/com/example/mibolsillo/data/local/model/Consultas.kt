package com.example.mibolsillo.data.local.model

import androidx.room.Embedded
import com.example.mibolsillo.data.local.entity.Categoria
import com.example.mibolsillo.data.local.entity.Movimiento

/*
 * Clases de resultado de consultas: NO son tablas, solo "moldes" donde Room
 * coloca el resultado de un SELECT con JOIN o con GROUP BY.
 */

/**
 * Un movimiento junto con su categoría (resultado de un JOIN).
 * @Embedded copia las columnas de cada clase; el prefijo "cat_" evita choques
 * entre columnas con el mismo nombre (ambas tablas tienen "id").
 */
data class MovimientoConCategoria(
    @Embedded val movimiento: Movimiento,
    @Embedded(prefix = "cat_") val categoria: Categoria
)

/** Semáforo del presupuesto: verde (< 80%), amarillo (80% a 99%), rojo (100% o más). */
enum class EstadoPresupuesto { NORMAL, ALERTA, EXCEDIDO }

/**
 * Un presupuesto con su categoría y lo que ya se gastó en ella ese mes.
 * "gastado" lo calcula SQL con una subconsulta sobre la tabla movimientos,
 * por eso la barra de progreso se actualiza sola al registrar un gasto.
 */
data class PresupuestoConGasto(
    val id: Long,
    val categoriaId: Long,
    val montoLimite: Double,
    val mes: Int,
    val anio: Int,
    val nombre: String,
    val icono: String,
    val color: Long,
    val gastado: Double
) {
    /** 0.85 = 85% del límite usado (puede pasar de 1.0). */
    val porcentaje: Double get() = if (montoLimite > 0) gastado / montoLimite else 0.0

    val restante: Double get() = montoLimite - gastado

    val estado: EstadoPresupuesto
        get() = when {
            porcentaje >= 1.0 -> EstadoPresupuesto.EXCEDIDO
            porcentaje >= 0.8 -> EstadoPresupuesto.ALERTA
            else -> EstadoPresupuesto.NORMAL
        }
}

/** Suma de montos de un tipo en un mes. [mes] viene como texto "2026-11". */
data class TotalMensual(
    val mes: String,
    val tipo: String,
    val total: Double
)

/** Suma de montos agrupada por categoría (para gráficas de pastel y presupuestos). */
data class TotalPorCategoria(
    val categoriaId: Long,
    val nombre: String,
    val icono: String,
    val color: Long,
    val total: Double
)
