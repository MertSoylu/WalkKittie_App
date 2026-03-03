package com.mert.paticat.di

import com.mert.paticat.data.repository.CatRepositoryImpl
import com.mert.paticat.data.repository.HealthRepositoryImpl
import com.mert.paticat.data.repository.InteractionRepositoryImpl
import com.mert.paticat.data.repository.MissionRepositoryImpl
import com.mert.paticat.data.repository.ShopRepositoryImpl
import com.mert.paticat.data.repository.UserProfileRepositoryImpl
import com.mert.paticat.domain.repository.CatRepository
import com.mert.paticat.domain.repository.HealthRepository
import com.mert.paticat.domain.repository.InteractionRepository
import com.mert.paticat.domain.repository.MissionRepository
import com.mert.paticat.domain.repository.ShopRepository
import com.mert.paticat.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindCatRepository(
        catRepositoryImpl: CatRepositoryImpl
    ): CatRepository
    
    @Binds
    @Singleton
    abstract fun bindHealthRepository(
        healthRepositoryImpl: HealthRepositoryImpl
    ): HealthRepository
    
    @Binds
    @Singleton
    abstract fun bindMissionRepository(
        missionRepositoryImpl: MissionRepositoryImpl
    ): MissionRepository
    
    @Binds
    @Singleton
    abstract fun bindShopRepository(
        shopRepositoryImpl: ShopRepositoryImpl
    ): ShopRepository
    
    @Binds
    @Singleton
    abstract fun bindInteractionRepository(
        interactionRepositoryImpl: InteractionRepositoryImpl
    ): InteractionRepository
    
    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository
}
