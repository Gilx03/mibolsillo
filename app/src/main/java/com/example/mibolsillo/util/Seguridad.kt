package com.example.mibolsillo.util

import java.security.MessageDigest

/** Utilidades de seguridad. */
object Seguridad {

    /**
     * Calcula el hash SHA-256 de la contraseña y lo devuelve en hexadecimal (64 caracteres).
     *
     * Usamos el correo como "sal" (salt): así dos usuarios con la misma contraseña
     * tendrán hashes distintos. El hash es de una sola vía: no se puede "descifrar",
     * solo comparar. Para iniciar sesión volvemos a calcular el hash y lo comparamos.
     */
    fun hashPassword(password: String, correo: String): String {
        val entrada = correo.trim().lowercase() + ":" + password
        val bytes = MessageDigest.getInstance("SHA-256").digest(entrada.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
