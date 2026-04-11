package com.viso.domain.model

enum class SalaryMode { SINGLE, SPLIT }

data class Config(
    val salaryCents: Long = 0L,
    val payday: Int = 5,
    val onboardingDone: Boolean = false,
    val notifDaysBefore: Int = 3,
    val lastResetMonth: String = "",
    val salaryMode: SalaryMode = SalaryMode.SINGLE,
    val salary1Cents: Long = 0L,
    val payday1: Int = 5,
    val salary2Cents: Long = 0L,
    val payday2: Int = 20
) {
    val effectiveSalaryCents: Long
        get() = if (salaryMode == SalaryMode.SPLIT) salary1Cents + salary2Cents else salaryCents
}
