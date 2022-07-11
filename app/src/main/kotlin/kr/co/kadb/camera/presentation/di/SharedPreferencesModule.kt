package kr.co.kadb.camera.presentation.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by oooobang on 2022. 5. 27..
 */
@Module
@InstallIn(SingletonComponent::class)
internal class SharedPreferencesModule {
	@Provides
	@Singleton
	fun provideSharedPreferences(application: Application): SharedPreferences {
		return application.getSharedPreferences(application.packageName, Context.MODE_PRIVATE)
	}
}
