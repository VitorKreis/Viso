package com.viso.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.viso.R
import com.viso.ui.theme.BgCard
import com.viso.ui.theme.TextPrimary
import com.viso.ui.theme.TextSecondary
import com.viso.ui.utils.formatCurrency
import java.time.YearMonth

class VisoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Usar prefs do app ou data store para obter dados reais
        // Por simplicidade, mostramos dados de exemplo/demonstração
        provideContent {
            VisoWidgetContent()
        }
    }
}

@Composable
private fun VisoWidgetContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BgCard)
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Viso - 70/20/10",
                style = TextStyle(
                    color = ColorProvider(TextPrimary),
                    fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(GlanceModifier.height(8.dp))

            Text(
                text = "Janeiro 2026",
                style = TextStyle(
                    color = ColorProvider(TextSecondary),
                    fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            )

            Spacer(GlanceModifier.height(16.dp))

            // 70%
            RuleBarWidget(label = "Contas", percentage = 70, amount = 350000L, color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF1565C0)))

            Spacer(GlanceModifier.height(8.dp))

            // 20%
            RuleBarWidget(label = "Poupança", percentage = 20, amount = 100000L, color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF2E7D32)))

            Spacer(GlanceModifier.height(8.dp))

            // 10%
            RuleBarWidget(label = "Extras", percentage = 10, amount = 50000L, color = ColorProvider(androidx.compose.ui.graphics.Color(0xFFEF6C00)))
        }
    }
}

@Composable
private fun RuleBarWidget(label: String, percentage: Int, amount: Long, color: ColorProvider) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label ($percentage%)",
            style = TextStyle(
                color = ColorProvider(TextPrimary),
                fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            text = formatCurrency(amount),
            style = TextStyle(
                color = color,
                fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp),
                fontWeight = FontWeight.Bold
            )
        )
    }
}
