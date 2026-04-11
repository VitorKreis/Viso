package com.viso.ui.utils

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(cents: Long): String {
    val value = cents.toBigDecimal().divide(BigDecimal(100))
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
}

fun maskCurrencyInput(input: String): Pair<Long, String> {
    val digits = input.filter { it.isDigit() }
    val cents = digits.toLongOrNull() ?: 0L
    return Pair(cents, formatCurrency(cents))
}
