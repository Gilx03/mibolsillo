package com.example.mibolsillo.util

/**
 * Reglas de validación de formularios.
 * Cada función devuelve null si el valor es válido, o el mensaje de error a mostrar.
 * Son funciones puras (sin Android), por eso se pueden probar con tests unitarios.
 */
object Validaciones {

    const val PASSWORD_MINIMO = 6

    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "Escribe tu nombre"
        nombre.trim().length < 2 -> "El nombre es muy corto"
        else -> null
    }

    fun validarCorreo(correo: String): String? = when {
        correo.isBlank() -> "Escribe tu correo"
        !REGEX_CORREO.matches(correo.trim()) -> "El correo no tiene un formato válido (ej: ana@correo.com)"
        else -> null
    }

    fun validarPassword(password: String): String? = when {
        password.isEmpty() -> "Escribe tu contraseña"
        password.length < PASSWORD_MINIMO -> "La contraseña debe tener al menos $PASSWORD_MINIMO caracteres"
        else -> null
    }

    fun validarConfirmacion(password: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Confirma tu contraseña"
        password != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    /**
     * Valida un monto escrito por el usuario. Acepta "1500", "1,500.50" o "1500,5".
     * Devuelve null si es válido.
     */
    fun validarMonto(texto: String): String? {
        val valor = parsearMonto(texto)
        return when {
            texto.isBlank() -> "Escribe un monto"
            valor == null -> "Monto no válido"
            valor <= 0.0 -> "El monto debe ser mayor que 0"
            valor > 999_999_999.0 -> "El monto es demasiado grande"
            else -> null
        }
    }

    /** Convierte el texto de un monto a Double, o null si no es un número válido. */
    fun parsearMonto(texto: String): Double? {
        val limpio = texto.trim().replace(" ", "")
        if (limpio.isEmpty()) return null
        // Si trae coma Y punto, la coma es separador de miles: "1,500.50"
        val normalizado = if (limpio.contains(',') && limpio.contains('.')) {
            limpio.replace(",", "")
        } else {
            limpio.replace(',', '.') // "1500,5" -> "1500.5"
        }
        return normalizado.toDoubleOrNull()
    }
}
