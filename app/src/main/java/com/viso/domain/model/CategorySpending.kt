package com.viso.domain.model

import androidx.compose.ui.graphics.Color

data class CategorySpending(
    val category: String,
    val amountCents: Long,
    val percentage: Float,
    val color: Color
)
