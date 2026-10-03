package com.viso.domain.usecase

import com.viso.domain.repository.BillRepositoryContract
import com.viso.domain.repository.GoalRepositoryContract
import com.viso.domain.repository.RemoteDataSource
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val billRepository: BillRepositoryContract,
    private val goalRepository: GoalRepositoryContract
) {
    suspend fun pullFromCloud() {
        // Pull bills
        remoteDataSource.getAllBillsOnce().onSuccess { cloudBills ->
            val localBills = billRepository.getAllBills()
            val cloudIds = cloudBills.map { it.id }.toSet()

            // Insert/update from cloud (cloud wins)
            cloudBills.forEach { cloudBill ->
                billRepository.insert(cloudBill)
            }

            // Upload local bills not in cloud
            localBills.filter { it.id !in cloudIds }.forEach { localBill ->
                remoteDataSource.syncBill(localBill)
            }
        }

        // Pull goals
        remoteDataSource.getAllGoalsOnce().onSuccess { cloudGoals ->
            val localGoals = goalRepository.getAllGoals()
            val cloudIds = cloudGoals.map { it.id }.toSet()

            // Insert/update from cloud (cloud wins)
            cloudGoals.forEach { cloudGoal ->
                goalRepository.insert(cloudGoal)
            }

            // Upload local goals not in cloud
            localGoals.filter { it.id !in cloudIds }.forEach { localGoal ->
                remoteDataSource.syncGoal(localGoal)
            }
        }
    }
}
