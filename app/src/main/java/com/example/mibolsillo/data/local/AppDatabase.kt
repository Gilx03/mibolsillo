package com.example.mibolsillo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mibolsillo.data.local.dao.CategoriaDao
import com.example.mibolsillo.data.local.dao.MetaAhorroDao
import com.example.mibolsillo.data.local.dao.MovimientoDao
import com.example.mibolsillo.data.local.dao.PresupuestoDao
import com.example.mibolsillo.data.local.dao.UsuarioDao
import com.example.mibolsillo.data.local.entity.Categoria
import com.example.mibolsillo.data.local.entity.MetaAhorro
import com.example.mibolsillo.data.local.entity.Movimiento
import com.example.mibolsillo.data.local.entity.Presupuesto
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.data.local.entity.Usuario

/**
 * Base de datos Room de la app. Declara las 5 tablas (entidades) y expone los DAOs.
 * Se usa como Singleton: solo existe una instancia en toda la app.
 */
@Database(
    entities = [Usuario::class, Categoria::class, Movimiento::class, Presupuesto::class, MetaAhorro::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun presupuestoDao(): PresupuestoDao
    abstract fun metaAhorroDao(): MetaAhorroDao

    companion object {
        private const val NOMBRE_BD = "mibolsillo.db"

        @Volatile
        private var instancia: AppDatabase? = null

        /** Devuelve la instancia única de la base de datos (patrón Singleton, seguro entre hilos). */
        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NOMBRE_BD)
                    .addCallback(PrecargaCallback)
                    .build()
                    .also { instancia = it }
            }

        /**
         * Se ejecuta UNA sola vez, cuando el archivo de la base de datos se crea por primera vez.
         * Inserta las categorías por defecto con SQL directo (ocurre antes de cualquier consulta).
         */
        private object PrecargaCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CategoriasIniciales.lista.forEach { c ->
                    db.execSQL(
                        "INSERT INTO categorias (nombre, icono, color, tipo) VALUES (?, ?, ?, ?)",
                        arrayOf<Any>(c.nombre, c.icono, c.color, c.tipo.name)
                    )
                }
            }
        }
    }
}

/** Categorías que se crean al instalar la app. Los iconos se resuelven en ui/components/IconosApp.kt. */
object CategoriasIniciales {
    val lista = listOf(
        // ---- Gastos ----
        Categoria(nombre = "Comida", icono = "comida", color = 0xFFFF7043, tipo = TipoMovimiento.GASTO),
        Categoria(nombre = "Transporte", icono = "transporte", color = 0xFF42A5F5, tipo = TipoMovimiento.GASTO),
        Categoria(nombre = "Estudios", icono = "estudios", color = 0xFF7E57C2, tipo = TipoMovimiento.GASTO),
        Categoria(nombre = "Entretenimiento", icono = "entretenimiento", color = 0xFFEC407A, tipo = TipoMovimiento.GASTO),
        Categoria(nombre = "Salud", icono = "salud", color = 0xFFEF5350, tipo = TipoMovimiento.GASTO),
        Categoria(nombre = "Hogar", icono = "hogar", color = 0xFF8D6E63, tipo = TipoMovimiento.GASTO),
        Categoria(nombre = "Ropa", icono = "ropa", color = 0xFF26C6DA, tipo = TipoMovimiento.GASTO),
        Categoria(nombre = "Servicios", icono = "servicios", color = 0xFFFFA726, tipo = TipoMovimiento.GASTO),
        Categoria(nombre = "Otros gastos", icono = "otros", color = 0xFF78909C, tipo = TipoMovimiento.GASTO),
        // ---- Ingresos ----
        Categoria(nombre = "Salario", icono = "salario", color = 0xFF43A047, tipo = TipoMovimiento.INGRESO),
        Categoria(nombre = "Mesada", icono = "mesada", color = 0xFF66BB6A, tipo = TipoMovimiento.INGRESO),
        Categoria(nombre = "Beca", icono = "beca", color = 0xFF26A69A, tipo = TipoMovimiento.INGRESO),
        Categoria(nombre = "Ventas / Freelance", icono = "ventas", color = 0xFF9CCC65, tipo = TipoMovimiento.INGRESO),
        Categoria(nombre = "Regalos", icono = "regalo", color = 0xFFAB47BC, tipo = TipoMovimiento.INGRESO),
        Categoria(nombre = "Otros ingresos", icono = "otros_ingresos", color = 0xFF8BC34A, tipo = TipoMovimiento.INGRESO)
    )
}
