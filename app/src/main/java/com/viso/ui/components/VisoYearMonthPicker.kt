package com.viso.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.Spacing
import java.time.YearMonth

@Composable
fun VisoYearMonthPicker(
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val ym = try { YearMonth.parse(initial) } catch (_: Exception) { YearMonth.now() }
    val selYear = remember { mutableStateOf(ym.year) }
    val selMonth = remember { mutableStateOf(ym.monthValue) }

    VisoBottomSheet(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Selecionar mês", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.md))

            VisoNumberPicker(value = selMonth.value, onValueChange = { selMonth.value = it }, range = 1..12, label = "Mês", displayTransform = { "%02d".format(it) })
            Spacer(Modifier.height(Spacing.sm))
            val currentYear = YearMonth.now().year
            VisoNumberPicker(value = selYear.value, onValueChange = { selYear.value = it }, range = (currentYear - 5)..(currentYear + 5), label = "Ano", displayTransform = { it.toString() })

            Spacer(Modifier.height(Spacing.lg))
            Button(onClick = { onConfirm(YearMonth.of(selYear.value, selMonth.value).toString()) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) {
                Text("Confirmar")
            }
            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}
