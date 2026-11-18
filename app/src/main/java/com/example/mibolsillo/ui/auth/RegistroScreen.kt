package com.example.mibolsillo.ui.auth

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
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
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit,
    viewModel: RegistroViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado = viewModel.uiState
    val focus = LocalFocusManager.current

    LaunchedEffect(estado.exito) {
        if (estado.exito) onRegistroExitoso()
    }

    PlantillaAuth(
        titulo = "Crea tu cuenta",
        subtitulo = "Empieza a controlar tu dinero hoy"
    ) {
        CampoTexto(
            valor = estado.nombre,
            onValorChange = viewModel::onNombreChange,
            etiqueta = "Nombre",
            icono = Icons.Filled.Person,
            error = estado.errorNombre
        )
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
            etiqueta = "Contraseña (mín. 6 caracteres)",
            icono = Icons.Filled.Lock,
            error = estado.errorPassword,
            tipoTeclado = KeyboardType.Password,
            esPassword = true
        )
        CampoTexto(
            valor = estado.confirmacion,
            onValorChange = viewModel::onConfirmacionChange,
            etiqueta = "Confirmar contraseña",
            icono = Icons.Filled.LockReset,
            error = estado.errorConfirmacion,
            tipoTeclado = KeyboardType.Password,
            accionIme = ImeAction.Done,
            onAccionIme = {
                focus.clearFocus()
                viewModel.registrar()
            },
            esPassword = true
        )
        Spacer(Modifier.height(12.dp))
        BotonPrincipal(
            texto = "Crear cuenta",
            cargando = estado.cargando,
            onClick = {
                focus.clearFocus()
                viewModel.registrar()
            }
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("¿Ya tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onIrALogin) { Text("Inicia sesión") }
        }
    }
}
