package com.viso.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }
        val cents = digits.toLongOrNull() ?: 0L
        val formatted = if (digits.isEmpty()) "" else formatCurrency(cents)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = formatted.length
            override fun transformedToOriginal(offset: Int) = text.length
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
