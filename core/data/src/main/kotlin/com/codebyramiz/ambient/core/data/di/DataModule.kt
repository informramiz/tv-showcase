package com.codebyramiz.ambient.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.codebyramiz.ambient.core.data.ClockRepository
import com.codebyramiz.ambient.core.data.DataStoreSettingsRepository
import com.codebyramiz.ambient.core.data.HttpFetcher
import com.codebyramiz.ambient.core.data.OpenMeteoWeatherRepository
import com.codebyramiz.ambient.core.data.SettingsRepository
import com.codebyramiz.ambient.core.data.SystemClockRepository
import com.codebyramiz.ambient.core.data.UrlConnectionFetcher
import com.codebyramiz.ambient.core.data.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds abstract fun settings(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds abstract fun weather(impl: OpenMeteoWeatherRepository): WeatherRepository

    @Binds abstract fun clock(impl: SystemClockRepository): ClockRepository

    @Binds abstract fun fetcher(impl: UrlConnectionFetcher): HttpFetcher
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun preferences(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("ambient_settings") }
}
