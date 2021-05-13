package com.justice.schoolmanagement.presentation.ui.admin

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.justice.schoolmanagement.presentation.ui.splash.SplashScreenActivity
import com.justice.schoolmanagement.presentation.ui.splash.adminData
import com.justice.schoolmanagement.utils.FirebaseUtil
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class AdminRepository(private val context: Context) {

    private val TAG = "AdminRepository"

    fun checkIfInstitutionCodeExits() = callbackFlow<Resource<String>> {
        val sharedPref = context.getSharedPreferences(SplashScreenActivity.SHARED_PREF, Context.MODE_PRIVATE)
        val adminData = sharedPref.adminData
        if (adminData == null) {
            offer(Resource.error(Exception("Code Does Not Exist")))
        } else {
            offer(Resource.success(adminData))
        }


        awaitClose { }
    }

    fun saveAdminDataInSharedPref(adminData: AdminData) {
        val sharedPref = context.getSharedPreferences(SplashScreenActivity.SHARED_PREF, Context.MODE_PRIVATE)
        val stringData = Gson().toJson(adminData)
        sharedPref.adminData = stringData

    }

    fun getAdminData(institutionCode: String) = callbackFlow<Resource<DocumentSnapshot>> {
        FirebaseUtil.getAdminData(institutionCode).addOnSuccessListener {
            offer(Resource.success(it))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }
        awaitClose { }
    }

    fun saveAdmin(adminData: AdminData) = callbackFlow<Resource<AdminData>> {

        FirebaseUtil.collectionReferenceAdmin.document(adminData.institutionCode).set(adminData).addOnSuccessListener {
            offer(Resource.success(adminData))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }
        awaitClose { }
    }

    fun checkIfUserIsSetup() = callbackFlow<Resource<DocumentSnapshot>> {
        Log.d(TAG, "checkIfUserIsSetup: ")
        FirebaseUtil.collectionReferenceTeachers().document(FirebaseUtil.getUid()).get().addOnSuccessListener {
            offer(Resource.success(it))

        }.addOnFailureListener {
            offer(Resource.error(it))
        }
        awaitClose { }
    }
}