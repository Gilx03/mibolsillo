package com.example.mibolsillo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "usuarios".
 * El correo tiene un índice ÚNICO: la base de datos impide registrar dos cuentas con el mismo correo.
 * Nunca guardamos la contraseña en texto plano, solo su hash SHA-256.
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String
)
