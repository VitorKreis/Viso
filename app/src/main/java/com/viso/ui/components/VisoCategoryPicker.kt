package com.viso.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.BgCard2
import com.viso.ui.theme.BorderSubtle
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextSecondary

val categoryIcon: Map<String, ImageVector> = mapOf(
    "moradia" to Icons.Rounded.Home,
    "alimentacao" to Icons.Rounded.ShoppingCart,
    "transporte" to Icons.Rounded.DirectionsCar,
    "saude" to Icons.Rounded.LocalHospital,
    "educacao" to Icons.Rounded.Book,
    "utilidade" to Icons.Rounded.Bolt,
    "lazer" to Icons.Rounded.MusicNote,
    "outro" to Icons.Rounded.MoreHoriz
)

val categoryDisplayName: Map<String, String> = mapOf(
    "moradia" to "Moradia",
    "alimentacao" to "Alimentação",
    "transporte" to "Transporte",
    "saude" to "Saúde",
    "educacao" to "Educação",
    "utilidade" to "Utilidade",
    "lazer" to "Lazer",
    "outro" to "Outro"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisoCategoryPicker(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = modifier
    ) {
        categoryIcon.keys.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(categoryDisplayName[category] ?: category) },
                leadingIcon = {
                    Icon(
                        categoryIcon[category]!!,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                    selectedLabelColor = AccentBlue,
                    selectedLeadingIconColor = AccentBlue,
                    containerColor = BgCard2,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == category,
                    selectedBorderColor = AccentBlue.copy(alpha = 0.4f),
                    borderColor = BorderSubtle
                )
            )
        }
    }
}
