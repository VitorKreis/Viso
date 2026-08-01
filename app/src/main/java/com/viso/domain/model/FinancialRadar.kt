package com.viso.domain.model

enum class RadarLevel { OK, ATTENTION, RISK, CRITICAL }

data class FinancialRadar(
    val level: RadarLevel,
    val title: String,
    val message: String,
    val action: String,
    val overLimitCents: Long = 0L,
    val newBillsCount: Int = 0,
    val installmentBillsCount: Int = 0,
    val emergencyCurrentCents: Long = 0L
) {
    val shouldShow: Boolean
        get() = level != RadarLevel.OK
}
