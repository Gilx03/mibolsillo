package com.example.mibolsillo.data.demo

import com.example.mibolsillo.data.local.AppDatabase
import com.example.mibolsillo.data.local.entity.MetaAhorro
import com.example.mibolsillo.data.local.entity.Movimiento
import com.example.mibolsillo.data.local.entity.Presupuesto
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Genera datos de ejemplo realistas para un estudiante (6 meses de movimientos,
 * presupuestos y metas). Sirve para la presentación: en segundos la app se ve "usada"
 * y se pueden mostrar las gráficas. Se activa desde Perfil > Cargar datos de demostración.
 */
class DatosDemo(private val db: AppDatabase) {

    /** Gasto típico por mes de cada categoría: (nombre, monto base, veces al mes). */
    private val gastosTipicos = listOf(
        Triple("Comida", 350.0, 6),
        Triple("Transporte", 300.0, 4),
        Triple("Estudios", 700.0, 1),
        Triple("Entretenimiento", 500.0, 2),
        Triple("Servicios", 900.0, 1),
        Triple("Ropa", 900.0, 1),
        Triple("Salud", 600.0, 1)
    )

    private val notas = mapOf(
        "Comida" to listOf("Almuerzo en la uni", "Cena con amigos", "Supermercado", "Picadera"),
        "Transporte" to listOf("Recarga OMSA", "Uber", "Pasaje guagua", "Gasolina"),
        "Estudios" to listOf("Libro de cálculo", "Copias", "Curso online"),
        "Entretenimiento" to listOf("Cine", "Concierto", "Videojuego"),
        "Servicios" to listOf("Plan del celular", "Internet"),
        "Ropa" to listOf("Tenis nuevos", "Camisa"),
        "Salud" to listOf("Farmacia", "Consulta")
    )

    /**
     * Carga los datos solo si la cuenta no tiene movimientos, para no mezclarlos
     * con datos reales ni duplicarlos. @return true si se cargaron.
     */
    suspend fun cargar(usuarioId: Long): Boolean {
        if (db.movimientoDao().contar(usuarioId) > 0) return false
        val random = Random(2026) // semilla fija: siempre genera los mismos datos
        val categorias = db.categoriaDao().obtenerTodas().associateBy { it.nombre }
        val hoy = LocalDate.now()
        val zona = ZoneId.systemDefault()
        fun millis(fecha: LocalDate) = fecha.atTime(LocalTime.of(8 + random.nextInt(12), random.nextInt(60)))
            .atZone(zona).toInstant().toEpochMilli()

        val movimientos = mutableListOf<Movimiento>()
        for (atras in 5 downTo 0) {
            val mes = YearMonth.from(hoy).minusMonths(atras.toLong())
            // En el mes actual solo generamos hasta el día de hoy
            val ultimoDia = if (atras == 0) hoy.dayOfMonth else mes.lengthOfMonth()
            fun diaAlAzar() = mes.atDay(1 + random.nextInt(ultimoDia))

            fun agregar(nombre: String, monto: Double, tipo: TipoMovimiento, fecha: LocalDate, nota: String) {
                val categoria = categorias[nombre] ?: return
                movimientos += Movimiento(
                    usuarioId = usuarioId,
                    categoriaId = categoria.id,
                    monto = (monto / 5).roundToInt() * 5.0, // montos "redondos" de 5 en 5
                    tipo = tipo,
                    fecha = millis(fecha),
                    nota = nota
                )
            }

            // Ingresos: mesada fija + trabajos freelance algunos meses
            agregar("Mesada", 8000.0, TipoMovimiento.INGRESO, mes.atDay(1), "Mesada del mes")
            if (random.nextBoolean()) {
                agregar("Ventas / Freelance", 1500.0 + random.nextInt(4) * 500, TipoMovimiento.INGRESO, diaAlAzar(), "Diseño para un cliente")
            }

            // Gastos de cada categoría con variación de ±40%
            gastosTipicos.forEach { (nombre, base, veces) ->
                repeat(veces) {
                    if (nombre in listOf("Ropa", "Salud") && random.nextInt(3) == 0) return@repeat
                    val monto = base * (0.6 + random.nextDouble() * 0.8)
                    agregar(nombre, monto, TipoMovimiento.GASTO, diaAlAzar(), notas[nombre]!!.random(random))
                }
            }
        }
        db.movimientoDao().insertarVarios(movimientos)

        // Presupuestos del mes actual
        val mesActual = YearMonth.from(hoy)
        listOf("Comida" to 3000.0, "Transporte" to 1200.0, "Entretenimiento" to 800.0, "Servicios" to 1000.0)
            .forEach { (nombre, limite) ->
                val categoria = categorias[nombre] ?: return@forEach
                db.presupuestoDao().guardar(
                    Presupuesto(
                        usuarioId = usuarioId,
                        categoriaId = categoria.id,
                        montoLimite = limite,
                        mes = mesActual.monthValue,
                        anio = mesActual.year
                    )
                )
            }

        // Metas de ahorro (una ya cumplida para mostrar ese estado)
        fun fecha(meses: Long) = hoy.plusMonths(meses).atTime(12, 0).atZone(zona).toInstant().toEpochMilli()
        listOf(
            MetaAhorro(usuarioId = usuarioId, nombre = "Laptop para la uni", montoObjetivo = 35000.0, montoActual = 12500.0, fechaLimite = fecha(6), icono = "laptop"),
            MetaAhorro(usuarioId = usuarioId, nombre = "Fondo de emergencia", montoObjetivo = 15000.0, montoActual = 4000.0, fechaLimite = fecha(10), icono = "emergencia"),
            MetaAhorro(usuarioId = usuarioId, nombre = "Viaje a Samaná", montoObjetivo = 8000.0, montoActual = 8000.0, fechaLimite = fecha(1), icono = "viaje")
        ).forEach { db.metaAhorroDao().insertar(it) }
        return true
    }
}
