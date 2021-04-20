package com.justice.schoolmanagement.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.justice.schoolmanagement.presentation.splash.SplashScreenRepository
import com.justice.schoolmanagement.presentation.ui.admin.AdminRepository
import com.justice.schoolmanagement.presentation.ui.parent.ParentRepository
import com.justice.schoolmanagement.presentation.ui.student.StudentsRepository
import com.justice.schoolmanagement.presentation.ui.teacher.TeacherRepository
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
    fun provideRepository(@ApplicationContext context: Context): ParentRepository {
        return ParentRepository(context)
    }

    @Provides
    @Singleton
    fun provideStudentRepo(@ApplicationContext context: Context): StudentsRepository {
        return StudentsRepository(context)
    }

    @Provides
    @Singleton
    fun provideTeacherRepo(@ApplicationContext context: Context): TeacherRepository {
        return TeacherRepository(context)
    }

    @Provides
    @Singleton
    fun provideAdminRepo(@ApplicationContext context: Context): AdminRepository {
        return AdminRepository(context)
    }

    @Provides
    @Singleton
    fun provideSplashScreenRepo(@ApplicationContext context: Context): SplashScreenRepository {
        return SplashScreenRepository(context)
    }

    @Provides
    @Singleton
    fun provideRequestManager(@ApplicationContext context: Context): RequestManager {
        return Glide.with(context)
    }
}