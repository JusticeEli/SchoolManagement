package com.justice.schoolmanagement.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.justice.schoolmanagement.presentation.ui.parent.ParentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRepository():ParentRepository{
        return  ParentRepository()
    }
    @Provides
    @Singleton
    fun provideRequestManager(@ApplicationContext context:Context):RequestManager{
        return Glide.with(context)
    }
}