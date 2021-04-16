package com.justice.schoolmanagement.presentation

import android.app.Application
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.justice.schoolmanagement.alldata.AllData
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import dagger.hilt.android.HiltAndroidApp
import java.util.*

@HiltAndroidApp
class SchoolApplication : Application() {
    //check if current user is admin

    private val TAG = "ApplicationClass"


    override fun onCreate() {
        super.onCreate()
        //loadTeacherNames()
    }

    fun loadTeacherNames() {
        teacherNames.clear()
        AllData.teacherDataList.clear()
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS).get().addOnCompleteListener { task ->
            //   Toast.makeText(this@ApplicationClass, "Loading Teachers name: ", Toast.LENGTH_SHORT).show()
            if (task.isSuccessful) {
                teacherNames.clear()
                for (documentSnapshot in task.result!!) {
                    teacherNames.add(documentSnapshot.toObject(TeacherData::class.java).fullName)
                    AllData.teacherDataList.add(documentSnapshot.toObject(TeacherData::class.java))
                }
            } else {
                Toast.makeText(this@SchoolApplication, "Error: " + task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
         private const val APPLICATION_ID = "AEBEC993-390A-CC14-FF29-1A31BA9A7000"
        private const val API_KEY = "88CE15DA-73EF-4399-82FD-AD69D4C3412C"
        private const val SERVER_URL = "https://api.backendless.com"

        var documentSnapshot: DocumentSnapshot? = null

        @JvmField
        var studentSnapshot: DocumentSnapshot?=null

        @JvmField
        var teacherData: TeacherData? = null

        @JvmField
        var teacherNames: MutableList<String> = ArrayList()

    }
}