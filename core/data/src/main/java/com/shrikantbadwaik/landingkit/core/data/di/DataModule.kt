package com.shrikantbadwaik.landingkit.core.data.di

import android.content.Context
import androidx.room.Room
import com.shrikantbadwaik.landingkit.core.data.ChecklistTemplateRepository
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.core.data.local.dao.ChecklistTemplateDao
import com.shrikantbadwaik.landingkit.core.data.local.dao.TripDao
import com.shrikantbadwaik.landingkit.core.data.local.database.MIGRATION_1_2
import com.shrikantbadwaik.landingkit.core.data.local.database.LandingKitDatabase
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalChecklistTemplateRepository
import com.shrikantbadwaik.landingkit.core.data.local.repository.LocalTripRepository
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): LandingKitDatabase = Room.databaseBuilder(
        context,
        LandingKitDatabase::class.java,
        "landing_kit.db",
    ).addMigrations(MIGRATION_1_2).build()

    @Provides
    fun provideTripDao(database: LandingKitDatabase): TripDao = database.tripDao()

    @Provides
    fun provideChecklistTemplateDao(
        database: LandingKitDatabase,
    ): ChecklistTemplateDao = database.checklistTemplateDao()

    @Provides
    @Singleton
    fun provideTripRepository(
        tripDao: TripDao,
    ): TripRepository = LocalTripRepository(tripDao)

    @Provides
    @Singleton
    fun provideChecklistTemplateRepository(
        dao: ChecklistTemplateDao,
    ): ChecklistTemplateRepository = LocalChecklistTemplateRepository(dao)

    @Provides
    @Singleton
    fun provideUserPreferences(
        @ApplicationContext context: Context,
    ): UserPreferences = UserPreferences(context)
}
