package com.justice.schoolmanagement.presentation

import android.app.Application
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import dagger.hilt.android.HiltAndroidApp
import java.util.*

@HiltAndroidApp
class SchoolApplication : Application() {
    //check if current user is admin
    private val TAG = "ApplicationClass"
    companion object {

        var documentSnapshot: DocumentSnapshot? = null

        @JvmField
        var studentSnapshot: DocumentSnapshot?=null

        @JvmField
        var teacherData: TeacherData? = null

        @JvmField
        var teacherNames: MutableList<String> = ArrayList()

    }
}