package com.viso.domain.model

import java.time.YearMonth
import java.util.UUID

/**
 * Contract shared by local persistence, remote adapters and month-related use cases.
 *
 * An empty month is intentional: it means that a bill has no explicit occurrence
 * month yet (the legacy/recurring case). It is different from an invalid month.
 */
object MonthContract {
    const val EMPTY = ""

    fun normalize(value: String, field: String = "month"): String {
        val normalized = value.trim()
        if (normalized.isEmpty()) return EMPTY

        return runCatching { YearMonth.parse(normalized).toString() }
            .getOrElse {
                throw IllegalArgumentException("$field must use YYYY-MM or be empty")
            }
    }

    fun requireMonth(value: String, field: String = "month"): String =
        normalize(value, field).also {
            require(it.isNotEmpty()) { "$field cannot be empty" }
        }

    fun resolveDueMonth(
        dueMonth: String,
        paidMonth: String,
        fallback: YearMonth
    ): YearMonth {
        val explicitDueMonth = normalize(dueMonth, "dueMonth")
        if (explicitDueMonth.isNotEmpty()) return YearMonth.parse(explicitDueMonth)

        // paidMonth is only a legacy fallback. A populated dueMonth always wins,
        // which preserves advance payments without moving the occurrence.
        val legacyPaidMonth = normalize(paidMonth, "paidMonth")
        return legacyPaidMonth.takeIf { it.isNotEmpty() }?.let(YearMonth::parse) ?: fallback
    }
}

object IdentityContract {
    fun requireId(id: String, field: String = "id"): String = id.trim().also {
        require(it.isNotEmpty()) { "$field cannot be empty" }
    }

    fun newId(): String = UUID.randomUUID().toString()
}
