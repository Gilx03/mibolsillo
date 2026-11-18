package com.example.mibolsillo.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Utilidades de fechas con java.time.
 * En la base de datos las fechas se guardan como milisegundos (Long);
 * aquí convertimos entre esos milisegundos y fechas legibles.
 */
object Fechas {

    private val espanol: Locale = Locale.forLanguageTag("es-DO")
    private val zona: ZoneId get() = ZoneId.systemDefault()
    private val formatoCorto = DateTimeFormatter.ofPattern("d MMM yyyy", espanol)
    private val formatoDia = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", espanol)

    /** Primer milisegundo del mes (día 1 a las 00:00). */
    fun inicioMes(mes: YearMonth): Long =
        mes.atDay(1).atStartOfDay(zona).toInstant().toEpochMilli()

    /** Último milisegundo del mes (justo antes del día 1 del mes siguiente). */
    fun finMes(mes: YearMonth): Long =
        mes.plusMonths(1).atDay(1).atStartOfDay(zona).toInstant().toEpochMilli() - 1

    fun aLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zona).toLocalDate()

    /** "Noviembre 2026" */
    fun nombreMes(mes: YearMonth): String =
        mes.month.getDisplayName(TextStyle.FULL_STANDALONE, espanol).replaceFirstChar { it.uppercase() } +
            " " + mes.year

    /** "Nov" (para etiquetas de gráficas) */
    fun mesCorto(mes: YearMonth): String =
        mes.month.getDisplayName(TextStyle.SHORT_STANDALONE, espanol).replace(".", "")
            .replaceFirstChar { it.uppercase() }

    /** "18 nov 2026" */
    fun formatoCorto(millis: Long): String = aLocalDate(millis).format(formatoCorto).replace(".", "")

    /** "Hoy", "Ayer" o "Lunes 16 de noviembre" (encabezados de la lista de movimientos). */
    fun etiquetaDia(fecha: LocalDate): String {
        val hoy = LocalDate.now(zona)
        return when (fecha) {
            hoy -> "Hoy"
            hoy.minusDays(1) -> "Ayer"
            else -> fecha.format(formatoDia).replaceFirstChar { it.uppercase() }
        }
    }

    // ---- Conversión para el DatePicker de Material 3 (trabaja en UTC) ----

    /** Fecha local guardada -> milisegundos UTC que entiende el DatePicker. */
    fun aMillisDatePicker(millis: Long): Long =
        aLocalDate(millis).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun hoyMillisDatePicker(): Long =
        LocalDate.now(zona).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    /**
     * Día elegido en el DatePicker -> milisegundos locales.
     * Le ponemos la hora actual para que los movimientos del mismo día queden ordenados.
     */
    fun desdeDatePicker(utcMillis: Long): Long {
        val dia = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
        return dia.atTime(LocalTime.now(zona)).atZone(zona).toInstant().toEpochMilli()
    }
}
