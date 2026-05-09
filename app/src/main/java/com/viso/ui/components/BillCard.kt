package com.viso.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.viso.domain.model.Bill
import com.viso.domain.usecase.BillStatus
import com.viso.domain.usecase.getBillStatus
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.AccentGreen
import com.viso.ui.theme.AccentRed
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency
import java.time.LocalDate

val categoryIcons = mapOf(
    "moradia" to Icons.Rounded.Home,
    "alimentacao" to Icons.Rounded.ShoppingCart,
    "transporte" to Icons.Rounded.DirectionsCar,
    "saude" to Icons.Rounded.Favorite,
    "educacao" to Icons.Rounded.MenuBook,
    "utilidade" to Icons.Rounded.Bolt,
    "lazer" to Icons.Rounded.MusicNote,
    "outro" to Icons.Rounded.MoreHoriz
)

fun getCategoryIcon(category: String): ImageVector =
    categoryIcons[category.lowercase()] ?: Icons.Rounded.MoreHoriz

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillCard(
    bill: Bill,
    onPaid: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val status = getBillStatus(bill, today)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onPaid()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> AccentGreen
                    SwipeToDismissBoxValue.EndToStart -> AccentRed
                    else -> BgCard
                },
                label = "swipeBg"
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Rounded.Check
                SwipeToDismissBoxValue.EndToStart -> Icons.Rounded.Delete
                else -> Icons.Rounded.MoreHoriz
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(color)
                    .padding(horizontal = Spacing.lg),
                contentAlignment = alignment
            ) {
                Icon(icon, contentDescription = null, tint = TextPrimary)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(BgCard)
                .padding(Spacing.md)
                .clickable { onEdit() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(AccentBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getCategoryIcon(bill.category),
                    contentDescription = bill.category,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Show installment info if applicable
                val subtitle = if (bill.isInstallment && bill.installmentNumber != null && bill.totalInstallments != null) {
                    "Parcela ${bill.installmentNumber}/${bill.totalInstallments} · Dia ${bill.dueDay}"
                } else {
                    "Dia ${bill.dueDay} · ${bill.category}"
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (bill.isInstallment) {
                        Icon(
                            imageVector = Icons.Rounded.Payments,
                            contentDescription = "Parcela",
                            tint = AccentBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = formatCurrency(bill.amountCents),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusBadge(status = status)
            }
            // Actions: mark paid, delete
            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    IconButton(onClick = { onPaid() }) {
                        Icon(Icons.Rounded.Check, contentDescription = "Marcar como paga", tint = AccentGreen)
                    }
                    IconButton(onClick = { onDelete() }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Excluir", tint = AccentRed)
                    }
                }
            }
        }
    }
}
