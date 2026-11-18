package com.example.mibolsillo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mibolsillo.data.preferencias.ModoTema
import com.example.mibolsillo.data.preferencias.PreferenciasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel de la Activity: expone las preferencias que afectan a TODA la app
 * (modo de tema y símbolo de moneda).
 */
class MainViewModel(preferencias: PreferenciasRepository) : ViewModel() {

    val modoTema: StateFlow<ModoTema> = preferencias.modoTema.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ModoTema.SISTEMA
    )

    val moneda: StateFlow<String> = preferencias.moneda.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PreferenciasRepository.MONEDA_POR_DEFECTO
    )
}
