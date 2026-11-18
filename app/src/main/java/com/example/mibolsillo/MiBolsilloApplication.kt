package com.example.mibolsillo

import android.app.Application
import com.example.mibolsillo.data.demo.DatosDemo
import com.example.mibolsillo.data.local.AppDatabase
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import com.example.mibolsillo.data.repository.CategoriaRepository
import com.example.mibolsillo.data.repository.MetaRepository
import com.example.mibolsillo.data.repository.MovimientoRepository
import com.example.mibolsillo.data.repository.PresupuestoRepository
import com.example.mibolsillo.data.repository.UsuarioRepository

/**
 * Clase Application: se crea una sola vez al abrir la app, antes que cualquier pantalla.
 * La usamos como "contenedor de dependencias" manual (inyección de dependencias sencilla,
 * sin librerías como Hilt): aquí se crean la base de datos y los repositorios,
 * y los ViewModels los piden desde AppViewModelProvider.
 */
class MiBolsilloApplication : Application() {

    // "by lazy" = se crea la primera vez que alguien lo usa, no antes.
    val baseDatos: AppDatabase by lazy { AppDatabase.obtener(this) }

    val preferencias: PreferenciasRepository by lazy { PreferenciasRepository(this) }

    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(baseDatos.usuarioDao(), preferencias)
    }

    val categoriaRepository: CategoriaRepository by lazy { CategoriaRepository(baseDatos.categoriaDao()) }

    val movimientoRepository: MovimientoRepository by lazy { MovimientoRepository(baseDatos.movimientoDao()) }

    val presupuestoRepository: PresupuestoRepository by lazy { PresupuestoRepository(baseDatos.presupuestoDao()) }

    val metaRepository: MetaRepository by lazy { MetaRepository(baseDatos.metaAhorroDao()) }

    val datosDemo: DatosDemo by lazy { DatosDemo(baseDatos) }
}
