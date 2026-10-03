package com.viso.domain.model

data class Bill(
    val id: String,
    val name: String,
    val amountCents: Long,
    val dueDay: Int,
    val category: String,
    val isPaid: Boolean,
    val paidMonth: String,
    val dueMonth: String = "",
    val createdAt: Long,
    val isRecurring: Boolean = false,
    val isInstallment: Boolean = false,
    val installmentNumber: Int? = null,
    val totalInstallments: Int? = null,
    val parentInstallmentId: String? = null
) {
    init {
        IdentityContract.requireId(id)
        require(name.isNotBlank()) { "name cannot be empty" }
        require(amountCents >= 0) { "amountCents cannot be negative" }
        require(dueDay in 1..31) { "dueDay must be between 1 and 31" }
        MonthContract.normalize(paidMonth, "paidMonth")
        MonthContract.normalize(dueMonth, "dueMonth")
        if (isPaid) {
            require(paidMonth.isNotBlank()) { "a paid bill must have paidMonth" }
        } else {
            require(paidMonth.isBlank()) { "an unpaid bill cannot have paidMonth" }
        }
    }
}
