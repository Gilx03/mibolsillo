package com.example.mibolsillo.data.local.entity

/**
 * Tipo de un movimiento o de una categoría.
 * Room guarda los enums como texto ("INGRESO" / "GASTO") automáticamente.
 */
enum class TipoMovimiento {
    INGRESO,
    GASTO
}
