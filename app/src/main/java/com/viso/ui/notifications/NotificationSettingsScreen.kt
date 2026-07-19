package com.viso.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.viso.ui.components.VisoNumberPicker
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.BgApp
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgApp,
        topBar = {
            TopAppBar(
                title = { Text("Notificações", style = MaterialTheme.typography.headlineMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgApp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Ativar/desativar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Lembretes de contas", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("Receber notificações antes do vencimento", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Switch(
                    checked = state.enabled,
                    onCheckedChange = { viewModel.onEnabledChange(it) }
                )
            }

            if (state.enabled) {
                Spacer(Modifier.height(Spacing.md))

                // Dias antes
                Text("Avisar com quantos dias de antecedência?", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(Spacing.sm))
                VisoNumberPicker(
                    value = state.daysBefore,
                    onValueChange = { viewModel.onDaysBeforeChange(it) },
                    range = 1..7,
                    label = "Dias de antecedência",
                    displayTransform = { "$it dia${if (it > 1) "s" else ""}" }
                )

                Spacer(Modifier.height(Spacing.md))

                // Horário
                Text("Horário do lembrete", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(Spacing.sm))
                VisoNumberPicker(
                    value = state.hour,
                    onValueChange = { viewModel.onHourChange(it) },
                    range = 6..22,
                    label = "Hora",
                    displayTransform = { String.format("%02d:00", it) }
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.saveSettings()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Salvar configurações")
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}
