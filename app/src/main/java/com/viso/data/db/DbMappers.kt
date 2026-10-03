package com.viso.data.db

import com.viso.data.db.entity.BillEntity
import com.viso.data.db.entity.MonthHistoryEntity
import com.viso.data.db.entity.PaymentHistoryEntity
import com.viso.domain.model.Bill
import com.viso.domain.model.MonthContract
import com.viso.domain.model.PaymentHistory
import com.viso.domain.repository.MonthHistoryRecord

internal fun BillEntity.toDomainBill() = Bill(
    id = id,
    name = name,
    amountCents = amountCents,
    dueDay = dueDay,
    category = category,
    isPaid = isPaid,
    paidMonth = paidMonth,
    dueMonth = dueMonth,
    createdAt = createdAt,
    isRecurring = isRecurring,
    isInstallment = isInstallment,
    installmentNumber = installmentNumber,
    totalInstallments = totalInstallments,
    parentInstallmentId = parentInstallmentId
)

internal fun Bill.toEntity() = BillEntity(
    id = id,
    name = name,
    amountCents = amountCents,
    dueDay = dueDay,
    category = category,
    isPaid = isPaid,
    paidMonth = paidMonth,
    dueMonth = dueMonth,
    createdAt = createdAt,
    isRecurring = isRecurring,
    isInstallment = isInstallment,
    installmentNumber = installmentNumber,
    totalInstallments = totalInstallments,
    parentInstallmentId = parentInstallmentId
)

internal fun PaymentHistoryEntity.toDomainPaymentHistory() = PaymentHistory(
    id = id,
    month = month,
    billId = billId,
    billName = billName,
    amountCents = amountCents,
    category = category,
    dueDay = dueDay,
    paidAt = paidAt,
    isRecurring = isRecurring
)

internal fun PaymentHistory.toEntity() = PaymentHistoryEntity(
    id = id,
    month = MonthContract.requireMonth(month),
    billId = billId,
    billName = billName,
    amountCents = amountCents,
    category = category,
    dueDay = dueDay,
    paidAt = paidAt,
    isRecurring = isRecurring
)

internal fun MonthHistoryEntity.toRecord() = MonthHistoryRecord(
    month = month,
    salaryCents = salaryCents,
    totalBillsCents = totalBillsCents,
    billsLimitCents = billsLimitCents,
    spendingBudgetCents = spendingBudgetCents,
    savingsBudgetCents = savingsBudgetCents
)

internal fun MonthHistoryRecord.toEntity() = MonthHistoryEntity(
    month = month,
    salaryCents = salaryCents,
    totalBillsCents = totalBillsCents,
    billsLimitCents = billsLimitCents,
    spendingBudgetCents = spendingBudgetCents,
    savingsBudgetCents = savingsBudgetCents
)
