package com.example.mibolsillo.data.preferencias

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Crea UN archivo DataStore llamado "preferencias" asociado al Context de la app. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias")

/** Modo de tema elegido por el usuario en Perfil. */
enum class ModoTema { SISTEMA, CLARO, OSCURO }

/**
 * Guarda datos pequeños clave-valor que deben sobrevivir al cerrar la app:
 *  - id del usuario con sesión iniciada
 *  - moneda preferida (RD$ por defecto)
 *  - modo de tema (claro / oscuro / sistema)
 *
 * Todo se expone como Flow, así la UI reacciona sola a los cambios.
 */
class PreferenciasRepository(private val context: Context) {

    private object Claves {
        val USUARIO_ID = longPreferencesKey("usuario_id_sesion")
        val MONEDA = stringPreferencesKey("moneda")
        val MODO_TEMA = stringPreferencesKey("modo_tema")
    }

    companion object {
        const val MONEDA_POR_DEFECTO = "RD$"
    }

    /** Id del usuario con sesión activa, o null si nadie ha iniciado sesión. */
    val usuarioIdSesion: Flow<Long?> = context.dataStore.data.map { it[Claves.USUARIO_ID] }

    val moneda: Flow<String> = context.dataStore.data.map { it[Claves.MONEDA] ?: MONEDA_POR_DEFECTO }

    val modoTema: Flow<ModoTema> = context.dataStore.data.map { prefs ->
        prefs[Claves.MODO_TEMA]?.let { runCatching { ModoTema.valueOf(it) }.getOrNull() } ?: ModoTema.SISTEMA
    }

    suspend fun guardarSesion(usuarioId: Long) {
        context.dataStore.edit { it[Claves.USUARIO_ID] = usuarioId }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { it.remove(Claves.USUARIO_ID) }
    }

    suspend fun guardarMoneda(simbolo: String) {
        context.dataStore.edit { it[Claves.MONEDA] = simbolo }
    }

    suspend fun guardarModoTema(modo: ModoTema) {
        context.dataStore.edit { it[Claves.MODO_TEMA] = modo.name }
    }
}
