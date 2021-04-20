package com.justice.schoolmanagement.presentation.splash


import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.splash.SplashScreenActivity.Companion.SHARED_PREF
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


class SplashScreenRepository(private val context: Context) {

    private val TAG = "SplashScreenRepository"

    fun checkIsUserLoggedIn() = callbackFlow<Resource<FirebaseUser>> {
        Log.d(TAG, "checkIsUserLoggedIn: ")

        offer(Resource.loading(""))
        if (FirebaseUtil.isUserLoggedIn) {
            offer(Resource.success(FirebaseUtil.firebaseAuth.currentUser!!))
        } else {
            offer(Resource.error(Exception("User Not Logged In")))
        }

        awaitClose { }
    }

    fun checkIfInstitutionCodeExits() = callbackFlow<Resource<String>> {
        Log.d(TAG, "checkIfInstitutionCodeExits: ")
        val sharedPref = context.getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        val adminData = sharedPref.adminData
        if (adminData == null) {
            offer(Resource.error(java.lang.Exception("Code Does Not Exist")))
        } else {
            offer(Resource.success(adminData))
        }


        awaitClose { }
    }

    fun checkIfUserIsSetup() = callbackFlow<Resource<DocumentSnapshot>> {
        Log.d(TAG, "checkIfUserIsSetup: ")
        FirebaseUtil.collectionReferenceTeachers.document(FirebaseUtil.getUid()).get().addOnSuccessListener {
            offer(Resource.success(it))

        }.addOnFailureListener {
            offer(Resource.error(it))
        }
        awaitClose { }
    }
}