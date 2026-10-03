package com.viso.domain.model

data class ExtraIncome(
    val id: String,
    val name: String,
    val amountCents: Long,
    val month: String
) {
    init {
        IdentityContract.requireId(id)
        require(name.isNotBlank()) { "name cannot be empty" }
        require(amountCents >= 0) { "amountCents cannot be negative" }
        MonthContract.requireMonth(month)
    }
}
