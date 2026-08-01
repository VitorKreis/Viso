package com.viso.domain.usecase

import com.viso.domain.model.Bill
import com.viso.domain.model.FinancialRadar
import com.viso.domain.model.Goal
import com.viso.domain.model.RadarLevel
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

class CalculateFinancialRadarUseCase @Inject constructor() {
    operator fun invoke(
        rule: FinancialRule,
        bills: List<Bill>,
        goals: List<Goal>,
        month: YearMonth = YearMonth.now()
    ): FinancialRadar {
        val totalBills = bills.sumOf { it.amountCents }
        val overLimit = totalBills - rule.billsLimitCents
        val nonRecurringBills = bills.filter { !it.isRecurring && !it.isInstallment }
        val installmentBills = bills.filter { it.isInstallment }
        val newBillsThisMonth = bills.count { bill ->
            val createdMonth = YearMonth.from(
                Instant.ofEpochMilli(bill.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            )
            createdMonth == month
        }
        val emergencyFund = goals.find { it.isEmergencyFund }
        val emergencyCurrent = emergencyFund?.currentAmountCents ?: 0L
        val emergencyTarget = emergencyFund?.targetAmountCents ?: 0L
        val emergencyMissing = emergencyTarget > 0 && emergencyCurrent <= 0L

        if (rule.totalCents > 0 && totalBills > rule.totalCents) {
            return FinancialRadar(
                level = RadarLevel.CRITICAL,
                title = "Gasto maior que a renda",
                message = "Suas contas ja passam da renda do mes. O problema deixou de ser margem e virou falta de caixa.",
                action = "Escolha uma conta avulsa para adiar ou renegociar antes de assumir outra despesa.",
                overLimitCents = overLimit.coerceAtLeast(0L),
                newBillsCount = newBillsThisMonth,
                installmentBillsCount = installmentBills.size,
                emergencyCurrentCents = emergencyCurrent
            )
        }

        if (overLimit > 0) {
            val cause = when {
                installmentBills.size >= 3 -> "As parcelas estao pesando mais que o normal."
                nonRecurringBills.size >= 3 -> "As contas avulsas deste mes se acumularam."
                else -> "O total de contas passou do limite de 70%."
            }
            return FinancialRadar(
                level = RadarLevel.RISK,
                title = "Limite estourado",
                message = "$cause Voce passou do limite de contas.",
                action = "Reduza ou adie pelo menos uma despesa antes de criar uma nova conta.",
                overLimitCents = overLimit,
                newBillsCount = newBillsThisMonth,
                installmentBillsCount = installmentBills.size,
                emergencyCurrentCents = emergencyCurrent
            )
        }

        if (emergencyMissing && rule.savingsCents > 0) {
            return FinancialRadar(
                level = RadarLevel.ATTENTION,
                title = "Reserva parada",
                message = "Sua reserva de emergencia ainda esta zerada, mesmo com bloco de poupanca previsto.",
                action = "Separe um valor minimo hoje, mesmo que seja menor que os 10% planejados.",
                newBillsCount = newBillsThisMonth,
                installmentBillsCount = installmentBills.size,
                emergencyCurrentCents = emergencyCurrent
            )
        }

        if (newBillsThisMonth >= 4 || installmentBills.size >= 4) {
            return FinancialRadar(
                level = RadarLevel.ATTENTION,
                title = "Muitas contas novas",
                message = "Este mes ganhou varias novas despesas. Esse e o padrao que costuma quebrar o planejamento.",
                action = "Revise as contas novas e marque uma para cortar, adiar ou substituir.",
                newBillsCount = newBillsThisMonth,
                installmentBillsCount = installmentBills.size,
                emergencyCurrentCents = emergencyCurrent
            )
        }

        return FinancialRadar(
            level = RadarLevel.OK,
            title = "Mes sob controle",
            message = "Suas contas estao dentro da regra 70-20-10.",
            action = "Mantenha o ritmo e preserve a reserva."
        )
    }
}
