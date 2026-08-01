package com.viso.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.viso.data.auth.AuthRepository
import com.viso.domain.model.Bill
import com.viso.domain.model.Goal
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    private val userId: String?
        get() = authRepository.getUserId()

    // BILLS
    suspend fun syncBill(bill: Bill): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("Usuário não autenticado"))
        
        return try {
            firestore.collection("users")
                .document(uid)
                .collection("bills")
                .document(bill.id)
                .set(bill.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBill(billId: String): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("Usuário não autenticado"))
        
        return try {
            firestore.collection("users")
                .document(uid)
                .collection("bills")
                .document(billId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncBillPaidStatus(billId: String, isPaid: Boolean, paidMonth: String): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            firestore.collection("users")
                .document(uid)
                .collection("bills")
                .document(billId)
                .update(mapOf(
                    "isPaid" to isPaid,
                    "paidMonth" to paidMonth,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllBillsOnce(): Result<List<Bill>> {
        val uid = userId ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("bills")
                .get()
                .await()
            val bills = snapshot.documents.mapNotNull { it.toBill() }
            Result.success(bills)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllGoalsOnce(): Result<List<Goal>> {
        val uid = userId ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("goals")
                .get()
                .await()
            val goals = snapshot.documents.mapNotNull { it.toGoal() }
            Result.success(goals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBillsFlow(): Flow<List<Bill>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            close(Exception("Usuário não autenticado"))
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(uid)
            .collection("bills")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val bills = snapshot?.documents?.mapNotNull { doc ->
                    doc.toBill()
                } ?: emptyList()

                trySend(bills)
            }

        awaitClose { listener.remove() }
    }

    // GOALS
    suspend fun syncGoal(goal: Goal): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("Usuário não autenticado"))
        
        return try {
            firestore.collection("users")
                .document(uid)
                .collection("goals")
                .document(goal.id)
                .set(goal.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGoal(goalId: String): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            firestore.collection("users")
                .document(uid)
                .collection("goals")
                .document(goalId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGoalsFlow(): Flow<List<Goal>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            close(Exception("Usuário não autenticado"))
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(uid)
            .collection("goals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { doc ->
                    doc.toGoal()
                } ?: emptyList()

                trySend(goals)
            }

        awaitClose { listener.remove() }
    }

    // Mappers
    private fun Bill.toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "amountCents" to amountCents,
        "dueDay" to dueDay,
        "category" to category,
        "isPaid" to isPaid,
        "paidMonth" to paidMonth,
        "dueMonth" to dueMonth,
        "createdAt" to createdAt,
        "isRecurring" to isRecurring,
        "isInstallment" to isInstallment,
        "installmentNumber" to (installmentNumber ?: 0),
        "totalInstallments" to (totalInstallments ?: 0),
        "parentInstallmentId" to (parentInstallmentId ?: ""),
        "updatedAt" to System.currentTimeMillis()
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toBill(): Bill? {
        return try {
            Bill(
                id = getString("id") ?: return null,
                name = getString("name") ?: "",
                amountCents = getLong("amountCents") ?: 0L,
                dueDay = (getLong("dueDay") ?: 1).toInt(),
                category = getString("category") ?: "outro",
                isPaid = getBoolean("isPaid") ?: false,
                paidMonth = getString("paidMonth") ?: "",
                dueMonth = getString("dueMonth") ?: "",
                createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
                isRecurring = getBoolean("isRecurring") ?: false,
                isInstallment = getBoolean("isInstallment") ?: false,
                installmentNumber = (getLong("installmentNumber") ?: 0).toInt().takeIf { it > 0 },
                totalInstallments = (getLong("totalInstallments") ?: 0).toInt().takeIf { it > 0 },
                parentInstallmentId = getString("parentInstallmentId").takeIf { !it.isNullOrEmpty() }
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Goal.toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "targetAmountCents" to targetAmountCents,
        "currentAmountCents" to currentAmountCents,
        "monthlyContributionCents" to monthlyContributionCents,
        "isEmergencyFund" to isEmergencyFund,
        "color" to color,
        "createdAt" to createdAt,
        "updatedAt" to System.currentTimeMillis()
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toGoal(): Goal? {
        return try {
            Goal(
                id = getString("id") ?: return null,
                name = getString("name") ?: "",
                targetAmountCents = getLong("targetAmountCents") ?: 0L,
                currentAmountCents = getLong("currentAmountCents") ?: 0L,
                monthlyContributionCents = getLong("monthlyContributionCents") ?: 0L,
                isEmergencyFund = getBoolean("isEmergencyFund") ?: false,
                color = getString("color") ?: "blue",
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
