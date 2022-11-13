package com.mnowo.transportationalarmclock.di

import com.mnowo.transportationalarmclock.data.GooglePlacesApi
import com.mnowo.transportationalarmclock.data.repository.AlarmClockRepositoryImpl
import com.mnowo.transportationalarmclock.domain.repository.AlarmClockRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGooglePlacesApi(retrofit: Retrofit) : GooglePlacesApi {
        return retrofit.create(GooglePlacesApi::class.java)
    }

    @Singleton
    @Provides
    fun provideAlarmClockRepository(
        googlePlacesApi: GooglePlacesApi
    ): AlarmClockRepository {
        return AlarmClockRepositoryImpl(
            googlePlacesApi = googlePlacesApi
        )
    }
}