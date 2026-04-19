package com.viso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.viso.data.datastore.ConfigDataStore
import com.viso.data.repository.BillRepository
import com.viso.data.repository.ExtraIncomeRepository
import com.viso.data.repository.GoalRepository
import com.viso.domain.usecase.CalculateRuleUseCase
import com.viso.domain.usecase.emergencyFundTarget
import com.viso.domain.model.Goal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import java.time.YearMonth
import java.util.UUID
import com.viso.ui.navigation.Screen
import com.viso.ui.navigation.VisoNavGraph
import com.viso.ui.theme.VisoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var configDataStore: ConfigDataStore
    @Inject
    lateinit var billRepo: BillRepository
    @Inject
    lateinit var extraRepo: ExtraIncomeRepository
    @Inject
    lateinit var goalRepo: GoalRepository
    @Inject
    lateinit var calculateRule: CalculateRuleUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure emergency fund exists on startup
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val existing = goalRepo.getEmergencyFund()
                if (existing == null) {
                    val bills = billRepo.getAllBills()
                    val totalBills = bills.sumOf { it.amountCents }
                    val emergencyTarget = emergencyFundTarget(totalBills)
                    val currentMonth = YearMonth.now().toString()
                    val extraTotal = try { extraRepo.getTotalForMonth(currentMonth) } catch (t: Throwable) { 0L }
                    val config = try { configDataStore.getConfig() } catch (t: Throwable) { com.viso.domain.model.Config() }
                    val rule = calculateRule(config.effectiveSalaryCents, extraTotal)
                    val savingsBudget = rule.savingsCents
                    goalRepo.insert(
                        Goal(
                            id = UUID.randomUUID().toString(),
                            name = "Reserva de emergência",
                            targetAmountCents = emergencyTarget,
                            currentAmountCents = 0L,
                            monthlyContributionCents = savingsBudget,
                            isEmergencyFund = true,
                            color = "teal",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            } catch (_: Exception) {
                // silently ignore; startup should not crash
            }
        }

        setContent {
            val startDestination by produceState<String?>(initialValue = null) {
                val config = configDataStore.getConfig()
                value = if (config.onboardingDone) Screen.Home.route else Screen.Onboarding.route
            }

            VisoTheme {
                startDestination?.let { dest ->
                    VisoNavGraph(startDestination = dest)
                }
            }
        }
    }
}
