package com.viso.domain.usecase

import com.viso.data.repository.BillRepository
import com.viso.data.repository.GoalRepository
import com.viso.data.sync.FirestoreSyncManager
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val syncManager: FirestoreSyncManager,
    private val billRepository: BillRepository,
    private val goalRepository: GoalRepository
) {
    suspend fun pullFromCloud() {
        // Pull bills
        syncManager.getAllBillsOnce().onSuccess { cloudBills ->
            val localBills = billRepository.getAllBills()
            val cloudIds = cloudBills.map { it.id }.toSet()

            // Insert/update from cloud (cloud wins)
            cloudBills.forEach { cloudBill ->
                billRepository.insert(cloudBill)
            }

            // Upload local bills not in cloud
            localBills.filter { it.id !in cloudIds }.forEach { localBill ->
                syncManager.syncBill(localBill)
            }
        }

        // Pull goals
        syncManager.getAllGoalsOnce().onSuccess { cloudGoals ->
            val localGoals = goalRepository.getAllGoals()
            val cloudIds = cloudGoals.map { it.id }.toSet()

            // Insert/update from cloud (cloud wins)
            cloudGoals.forEach { cloudGoal ->
                goalRepository.insert(cloudGoal)
            }

            // Upload local goals not in cloud
            localGoals.filter { it.id !in cloudIds }.forEach { localGoal ->
                syncManager.syncGoal(localGoal)
            }
        }
    }
}
