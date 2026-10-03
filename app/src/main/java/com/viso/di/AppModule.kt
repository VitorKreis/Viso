package com.viso.di

import android.content.Context
import androidx.room.Room
import com.viso.data.datastore.ConfigDataStore
import com.viso.data.db.VisoDB
import com.viso.data.db.VisoMigrations
import com.viso.data.db.dao.AchievementDao
import com.viso.data.db.dao.BillDao
import com.viso.data.db.dao.ExtraIncomeDao
import com.viso.data.db.dao.GoalDao
import com.viso.data.db.dao.InstallmentBillDao
import com.viso.data.db.dao.MonthHistoryDao
import com.viso.data.db.dao.PaymentHistoryDao
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
            .addMigrations(*VisoMigrations.ALL)
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
    fun provideInstallmentBillDao(db: VisoDB): InstallmentBillDao = db.installmentBillDao()

    @Provides
    fun provideAchievementDao(db: VisoDB): AchievementDao = db.achievementDao()

    @Provides
    fun providePaymentHistoryDao(db: VisoDB): PaymentHistoryDao = db.paymentHistoryDao()

    @Provides
    @Singleton
    fun provideConfigDataStore(@ApplicationContext context: Context): ConfigDataStore =
        ConfigDataStore(context)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth =
        com.google.firebase.auth.FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): com.google.firebase.firestore.FirebaseFirestore =
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
}
