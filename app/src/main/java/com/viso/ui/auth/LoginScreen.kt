package com.viso.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.viso.R
import com.viso.data.auth.AuthState
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.BgApp
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle(initialValue = AuthState.Loading)
    val snackbarHostState = remember { SnackbarHostState() }

    // Configurar Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("784359636706-2vg7gi222ohurjhjdd3otj91kb2fdag7.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    viewModel.onGoogleSignInResult(account)
                } catch (e: ApiException) {
                    viewModel.onGoogleSignInError("Erro ao fazer login: ${e.message}")
                }
            }
            Activity.RESULT_CANCELED -> {
                viewModel.onLoginCancelled()
            }
            else -> {
                viewModel.onGoogleSignInError("Erro desconhecido no login")
            }
        }
    }

    // Observar eventos
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.NavigateToHome -> onLoginSuccess()
                is LoginEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // Se já estiver autenticado, navega para home
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    Scaffold(
        containerColor = BgApp,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.lg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Logo/Icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = "Viso Logo",
                        modifier = Modifier.size(80.dp),
                        tint = AccentBlue
                    )
                }

                Spacer(Modifier.height(Spacing.lg))

                // Título
                Text(
                    text = "Bem-vindo ao Viso",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Organize suas finanças com inteligência",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(Spacing.xxxl))

                // Botão Principal - Entrar com Backup (Login Anônimo)
                Button(
                    onClick = { viewModel.signInAnonymously() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = TextPrimary
                        )
                    } else {
                        Text(
                            text = "☁️ Entrar com Backup na Nuvem",
                            color = TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.sm))

                // Botão Google Sign-In
                OutlinedButton(
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        launcher.launch(signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "G",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentBlue,
                        modifier = Modifier.padding(end = Spacing.md)
                    )
                    Text(
                        text = "Vincular com Google (backup seguro)",
                        color = AccentBlue
                    )
                }

                Spacer(Modifier.height(Spacing.md))

                // Botão pular (uso offline)
                OutlinedButton(
                    onClick = { viewModel.skipLogin() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "Usar offline (sem backup)",
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.height(Spacing.xl))

                // Texto explicativo
                Text(
                    text = "Com backup na nuvem, seus dados ficam salvos e você pode acessar de qualquer dispositivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                if (uiState.errorMessage != null) {
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
