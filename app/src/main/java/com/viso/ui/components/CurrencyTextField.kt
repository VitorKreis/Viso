package com.viso.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.BgInput    
import com.viso.ui.theme.TextMuted
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.CurrencyVisualTransformation

@Composable
fun CurrencyTextField(
    amountCents: Long,
    onAmountChange: (Long) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next
) {
    var textState by remember { mutableStateOf(if (amountCents > 0) amountCents.toString() else "") }

    OutlinedTextField(
        value = textState,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }.take(11)
            textState = digits
            onAmountChange(digits.toLongOrNull() ?: 0L)
        },
        label = { Text(label) },
        placeholder = { Text("R\$ 0,00", color = TextMuted) },
        visualTransformation = CurrencyVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = imeAction
        ),
        singleLine = true,
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = BgInput,
            focusedContainerColor = BgInput,
            unfocusedContainerColor = BgInput,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = AccentBlue,
            unfocusedLabelColor = TextSecondary,
            cursorColor = AccentBlue
        )
    )
}
