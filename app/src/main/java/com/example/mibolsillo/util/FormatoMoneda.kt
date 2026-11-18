package com.example.mibolsillo.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Da formato a cantidades de dinero: 1250.5 -> "RD$ 1,250.50". */
object FormatoMoneda {

    /** Monedas que el usuario puede elegir en Perfil (símbolo -> nombre). */
    val MONEDAS = listOf(
        "RD$" to "Peso dominicano",
        "US$" to "Dólar estadounidense",
        "€" to "Euro",
        "MX$" to "Peso mexicano",
        "COP$" to "Peso colombiano"
    )

    // Formato con separador de miles "," y decimales "." (estándar en República Dominicana).
    private val formato = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))

    /** Formato corto para ejes de gráficas: 950 -> "950", 1500 -> "1.5k", 2300000 -> "2.3M". */
    fun compacto(valor: Double): String {
        val abs = kotlin.math.abs(valor)
        val (numero, sufijo) = when {
            abs >= 1_000_000 -> valor / 1_000_000 to "M"
            abs >= 1_000 -> valor / 1_000 to "k"
            else -> valor to ""
        }
        val texto = if (numero % 1.0 == 0.0) numero.toLong().toString()
        else DecimalFormat("0.#", DecimalFormatSymbols(Locale.US)).format(numero)
        return texto + sufijo
    }

    fun formatear(monto: Double, simbolo: String = "RD$"): String {
        val signo = if (monto < 0) "-" else ""
        return "$signo$simbolo ${formato.format(kotlin.math.abs(monto))}"
    }
}
