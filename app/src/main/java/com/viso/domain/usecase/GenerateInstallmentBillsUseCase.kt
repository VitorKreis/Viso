package com.viso.domain.usecase

import com.viso.data.repository.BillRepository
import com.viso.data.repository.InstallmentBillRepository
import com.viso.domain.model.Bill
import com.viso.domain.model.InstallmentBill
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class GenerateInstallmentBillsUseCase @Inject constructor(
    private val installmentBillRepository: InstallmentBillRepository,
    private val billRepository: BillRepository
) {
    /**
     * Gera as contas mensais (Bill) para um novo parcelamento.
     * Deve ser chamado quando o usuário cria uma compra parcelada.
     */
    suspend fun generateInitialBill(installmentBill: InstallmentBill) {
        val currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val startMonth = YearMonth.parse(installmentBill.startMonth)
        val currentMonthParsed = YearMonth.parse(currentMonth)

        // Só gera a primeira parcela se o mês de início for o atual ou passado
        if (!startMonth.isAfter(currentMonthParsed)) {
            generateBillForMonth(installmentBill, 1, currentMonth)
        }
    }

    /**
     * Gera todas as parcelas pendentes para o mês atual.
     * Deve ser chamado no reset mensal.
     */
    suspend fun generateBillsForMonth(month: String) {
        val activeInstallments = installmentBillRepository.getAllActive()
        val targetMonth = YearMonth.parse(month)

        activeInstallments.forEach { installment ->
            val startMonth = YearMonth.parse(installment.startMonth)

            // Verifica se o parcelamento já deveria ter começado
            if (!startMonth.isAfter(targetMonth)) {
                // Calcula qual é o número da parcela para este mês
                val monthsDiff = (targetMonth.year - startMonth.year) * 12 +
                        (targetMonth.monthValue - startMonth.monthValue)
                val installmentNumber = monthsDiff + 1

                // Verifica se ainda há parcelas a gerar
                if (installmentNumber <= installment.totalInstallments) {
                    // Verifica se já não existe uma conta para esta parcela
                    val existingBills = billRepository.getInstallmentBillsByParentId(installment.id)
                    val alreadyExists = existingBills.any { it.paidMonth == month }

                    if (!alreadyExists) {
                        generateBillForMonth(installment, installmentNumber, month)
                    }
                } else {
                    // Todas as parcelas foram geradas, desativa o parcelamento
                    installmentBillRepository.deactivate(installment.id)
                }
            }
        }
    }

    /**
     * Calcula o valor de uma parcela específica.
     * A primeira parcela recebe o resto da divisão para garantir que some exatamente o total.
     */
    fun calculateInstallmentAmount(
        totalAmountCents: Long,
        totalInstallments: Int,
        installmentNumber: Int
    ): Long {
        val baseAmount = totalAmountCents / totalInstallments
        val remainder = totalAmountCents % totalInstallments

        return if (installmentNumber == 1) {
            baseAmount + remainder
        } else {
            baseAmount
        }
    }

    private suspend fun generateBillForMonth(
        installment: InstallmentBill,
        installmentNumber: Int,
        month: String
    ) {
        val amount = calculateInstallmentAmount(
            installment.totalAmountCents,
            installment.totalInstallments,
            installmentNumber
        )

        val bill = Bill(
            id = UUID.randomUUID().toString(),
            name = "${installment.name} - Parcela $installmentNumber/${installment.totalInstallments}",
            amountCents = amount,
            dueDay = installment.dueDay,
            category = installment.category,
            isPaid = false,
            paidMonth = "",
            createdAt = System.currentTimeMillis(),
            isRecurring = false,
            isInstallment = true,
            installmentNumber = installmentNumber,
            totalInstallments = installment.totalInstallments,
            parentInstallmentId = installment.id
        )

        billRepository.insert(bill)
    }
}
