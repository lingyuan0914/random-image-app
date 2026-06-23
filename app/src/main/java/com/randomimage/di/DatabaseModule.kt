package com.randomimage.di

import android.content.Context
import com.randomimage.data.local.AppDataStore
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDataStore(@ApplicationContext context: Context, moshi: Moshi): AppDataStore {
        return AppDataStore(context, moshi)
    }
}
