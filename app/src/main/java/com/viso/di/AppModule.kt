package com.viso.di

import android.content.Context
import androidx.room.Room
import com.viso.data.datastore.ConfigDataStore
import com.viso.data.db.VisoDB
import com.viso.data.db.dao.BillDao
import com.viso.data.db.dao.ExtraIncomeDao
import com.viso.data.db.dao.GoalDao
import com.viso.data.db.dao.MonthHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VisoDB =
        Room.databaseBuilder(context, VisoDB::class.java, "viso.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideBillDao(db: VisoDB): BillDao = db.billDao()

    @Provides
    fun provideGoalDao(db: VisoDB): GoalDao = db.goalDao()

    @Provides
    fun provideExtraIncomeDao(db: VisoDB): ExtraIncomeDao = db.extraIncomeDao()

    @Provides
    fun provideMonthHistoryDao(db: VisoDB): MonthHistoryDao = db.monthHistoryDao()

    @Provides
    @Singleton
    fun provideConfigDataStore(@ApplicationContext context: Context): ConfigDataStore =
        ConfigDataStore(context)
}
