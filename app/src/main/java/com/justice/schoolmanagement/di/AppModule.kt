package com.justice.schoolmanagement.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.justice.schoolmanagement.presentation.splash.SplashScreenRepository
import com.justice.schoolmanagement.presentation.ui.admin.AdminRepository
import com.justice.schoolmanagement.presentation.ui.attendance.AttendanceRepository
import com.justice.schoolmanagement.presentation.ui.blog.BlogRepository
import com.justice.schoolmanagement.presentation.ui.class_.ChoosenClassRepository
import com.justice.schoolmanagement.presentation.ui.fees.FeesRepository
import com.justice.schoolmanagement.presentation.ui.parent.ParentRepository
import com.justice.schoolmanagement.presentation.ui.register.RegisterRepository
import com.justice.schoolmanagement.presentation.ui.results.ResultsRepository
import com.justice.schoolmanagement.presentation.ui.student.StudentsRepository
import com.justice.schoolmanagement.presentation.ui.subjects.SubjectsRepository
import com.justice.schoolmanagement.presentation.ui.teacher.TeacherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideRepository(@ApplicationContext context: Context): ParentRepository {
        return ParentRepository(context)
    }

    @Provides
    @Singleton
    fun provideAttendanceRepo(@ApplicationContext context: Context): AttendanceRepository {
        return AttendanceRepository()
    }
    @Provides
    @Singleton
    fun provideResultsRepo(@ApplicationContext context: Context): ResultsRepository {
        return ResultsRepository()
    }
    @Provides
    @Singleton
    fun choosenClassRepo(@ApplicationContext context: Context): ChoosenClassRepository {
        return ChoosenClassRepository()
    }
    @Provides
    @Singleton
    fun provideSubjectsRepo(@ApplicationContext context: Context): SubjectsRepository {
        return SubjectsRepository()
    }

    @Provides
    @Singleton
    fun FeesRepo(@ApplicationContext context: Context): FeesRepository {
        return FeesRepository()
    }

    @Provides
    @Singleton
    fun provideRegisterRepo(@ApplicationContext context: Context): RegisterRepository {
        return RegisterRepository()
    }

    @Provides
    @Singleton
    fun provideBlogRepo(@ApplicationContext context: Context): BlogRepository {
        return BlogRepository()
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


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope