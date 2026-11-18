package com.example.mibolsillo.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.mibolsillo.data.local.dao.UsuarioDao
import com.example.mibolsillo.data.local.entity.Usuario
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import com.example.mibolsillo.util.Seguridad
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** Resultado de registrar o iniciar sesión. */
sealed interface ResultadoAuth {
    data class Exito(val usuario: Usuario) : ResultadoAuth
    data class Error(val mensaje: String) : ResultadoAuth
}

/**
 * Repositorio de usuarios y sesión.
 * Es el ÚNICO lugar que sabe cómo se registran usuarios, cómo se verifica una contraseña
 * y dónde se guarda la sesión. Los ViewModels solo llaman a estas funciones.
 */
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val preferencias: PreferenciasRepository
) {

    /** Usuario con sesión activa (null si no hay sesión). Se actualiza solo. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val usuarioActual: Flow<Usuario?> = preferencias.usuarioIdSesion.flatMapLatest { id ->
        if (id == null) flowOf(null) else usuarioDao.observarPorId(id)
    }

    /** Devuelve el id del usuario con sesión, comprobando que siga existiendo en la BD. */
    suspend fun obtenerSesionValida(): Long? {
        val id = preferencias.usuarioIdSesion.first() ?: return null
        return if (usuarioDao.buscarPorId(id) != null) id else {
            preferencias.cerrarSesion() // sesión "huérfana": la limpiamos
            null
        }
    }

    suspend fun registrar(nombre: String, correo: String, password: String): ResultadoAuth {
        val correoNormalizado = correo.trim().lowercase()

        if (usuarioDao.buscarPorCorreo(correoNormalizado) != null) {
            return ResultadoAuth.Error("Ya existe una cuenta con ese correo")
        }

        val usuario = Usuario(
            nombre = nombre.trim(),
            correo = correoNormalizado,
            passwordHash = Seguridad.hashPassword(password, correoNormalizado)
        )
        return try {
            val id = usuarioDao.insertar(usuario)
            preferencias.guardarSesion(id) // queda con la sesión iniciada al registrarse
            ResultadoAuth.Exito(usuario.copy(id = id))
        } catch (e: SQLiteConstraintException) {
            // Por si dos registros simultáneos pasan la comprobación anterior
            ResultadoAuth.Error("Ya existe una cuenta con ese correo")
        }
    }

    suspend fun iniciarSesion(correo: String, password: String): ResultadoAuth {
        val correoNormalizado = correo.trim().lowercase()
        val usuario = usuarioDao.buscarPorCorreo(correoNormalizado)
            ?: return ResultadoAuth.Error("No existe una cuenta con ese correo")

        val hash = Seguridad.hashPassword(password, correoNormalizado)
        if (hash != usuario.passwordHash) {
            return ResultadoAuth.Error("Contraseña incorrecta")
        }
        preferencias.guardarSesion(usuario.id)
        return ResultadoAuth.Exito(usuario)
    }

    suspend fun cerrarSesion() = preferencias.cerrarSesion()
}
