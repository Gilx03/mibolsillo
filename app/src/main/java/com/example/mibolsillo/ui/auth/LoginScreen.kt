package com.example.mibolsillo.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.components.BotonPrincipal
import com.example.mibolsillo.ui.components.CampoTexto

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado = viewModel.uiState
    val focus = LocalFocusManager.current

    // Cuando el ViewModel marca "exito", navegamos (solo una vez).
    LaunchedEffect(estado.exito) {
        if (estado.exito) onLoginExitoso()
    }

    PlantillaAuth(
        titulo = "¡Hola de nuevo!",
        subtitulo = "Inicia sesión para ver tus finanzas"
    ) {
        AnimatedVisibility(visible = estado.errorGeneral != null) {
            TarjetaError(estado.errorGeneral.orEmpty())
        }
        CampoTexto(
            valor = estado.correo,
            onValorChange = viewModel::onCorreoChange,
            etiqueta = "Correo electrónico",
            icono = Icons.Filled.Email,
            error = estado.errorCorreo,
            tipoTeclado = KeyboardType.Email
        )
        CampoTexto(
            valor = estado.password,
            onValorChange = viewModel::onPasswordChange,
            etiqueta = "Contraseña",
            icono = Icons.Filled.Lock,
            error = estado.errorPassword,
            tipoTeclado = KeyboardType.Password,
            accionIme = ImeAction.Done,
            onAccionIme = {
                focus.clearFocus()
                viewModel.iniciarSesion()
            },
            esPassword = true
        )
        Spacer(Modifier.height(12.dp))
        BotonPrincipal(
            texto = "Iniciar sesión",
            cargando = estado.cargando,
            onClick = {
                focus.clearFocus()
                viewModel.iniciarSesion()
            }
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onIrARegistro) { Text("Regístrate") }
        }
    }
}
