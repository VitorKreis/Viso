package com.viso.di

import com.viso.data.repository.BillRepository
import com.viso.data.repository.GoalRepository
import com.viso.data.repository.RoomMonthCloseStore
import com.viso.data.repository.MonthCloseSideEffectsImpl
import com.viso.data.sync.FirestoreSyncManager
import com.viso.domain.repository.BillRepositoryContract
import com.viso.domain.repository.GoalRepositoryContract
import com.viso.domain.repository.MonthCloseStore
import com.viso.domain.repository.ConfigRepositoryContract
import com.viso.domain.repository.MonthCloseSideEffects
import com.viso.domain.repository.RemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindBillRepository(repository: BillRepository): BillRepositoryContract

    @Binds
    @Singleton
    abstract fun bindGoalRepository(repository: GoalRepository): GoalRepositoryContract

    @Binds
    @Singleton
    abstract fun bindRemoteDataSource(dataSource: FirestoreSyncManager): RemoteDataSource

    @Binds
    @Singleton
    abstract fun bindMonthCloseStore(store: RoomMonthCloseStore): MonthCloseStore

    @Binds
    @Singleton
    abstract fun bindConfigRepository(repository: com.viso.data.repository.ConfigRepository): ConfigRepositoryContract

    @Binds
    @Singleton
    abstract fun bindMonthCloseSideEffects(sideEffects: MonthCloseSideEffectsImpl): MonthCloseSideEffects
}
