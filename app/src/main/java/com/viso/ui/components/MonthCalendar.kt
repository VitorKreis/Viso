package com.viso.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.viso.ui.theme.AccentBlue
import com.viso.ui.theme.BgCard2
import com.viso.ui.theme.Spacing
import com.viso.ui.theme.TextMuted
import com.viso.ui.theme.TextPrimary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class CalendarEvent(
    val day: Int,
    val color: Color,
    val label: String
)

@Composable
fun MonthCalendar(
    yearMonth: YearMonth,
    events: List<CalendarEvent>,
    selectedDay: Int?,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val firstDayOfWeek = remember(yearMonth) {
        yearMonth.atDay(1).dayOfWeek.value % 7 // Sunday = 0
    }
    val daysInMonth = remember(yearMonth) { yearMonth.lengthOfMonth() }
    val dayHeaders = listOf("D", "S", "T", "Q", "Q", "S", "S")
    val eventsByDay = remember(events) { events.groupBy { it.day } }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayHeaders.forEach { header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cellSize = (maxWidth / 7)

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = false
            ) {
                items(firstDayOfWeek) {
                    Box(modifier = Modifier.size(cellSize))
                }

                items(daysInMonth) { index ->
                    val day = index + 1
                    val isToday = yearMonth.year == today.year &&
                            yearMonth.monthValue == today.monthValue &&
                            day == today.dayOfMonth
                    val isSelected = day == selectedDay
                    val dayEvents = eventsByDay[day] ?: emptyList()

                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .clip(CircleShape)
                            .then(
                                if (isToday) Modifier.background(AccentBlue.copy(alpha = 0.25f))
                                else Modifier
                            )
                            .then(
                                if (isSelected) Modifier.border(1.5.dp, AccentBlue, CircleShape)
                                else Modifier
                            )
                            .clickable { onDayClick(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isToday) AccentBlue else TextPrimary
                            )
                            if (dayEvents.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    dayEvents.take(3).forEach { event ->
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                            .clip(CircleShape)
                                            .background(event.color)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}
