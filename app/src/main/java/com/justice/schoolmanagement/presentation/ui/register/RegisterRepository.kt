package com.justice.schoolmanagement.presentation.ui.register

import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import com.justice.schoolmanagement.presentation.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.*


class RegisterRepository {
    fun getCurrentDate() = callbackFlow<Resource<Date>> {
        offer(Resource.loading(""))
        FirebaseUtil.getCurrentDate {
            if (it == null) {
                val date = Calendar.getInstance().time
                offer(Resource.success(date))
            } else {
                offer(Resource.success(it))

            }
        }
        awaitClose { }
    }

    fun startFetchingData(currentInfo: CurrentInfo) = callbackFlow<Resource<DocumentSnapshot>> {

        FirebaseUtil.collectionReferenceDate().document(currentInfo.currentDate).get().addOnSuccessListener {
            if (it.exists()) {
                offer(Resource.success(it))
            } else {
                offer(Resource.empty())
            }

        }.addOnFailureListener {
            offer(Resource.error(it))
        }
        awaitClose { }
    }

    fun documentExist(currentInfo: CurrentInfo, snapshot: DocumentSnapshot) = callbackFlow<Resource<List<DocumentSnapshot>>> {
        val query: Query
        if (RegisterFragment.currentInfo.currentClassGrade.equals("all")) {
            query = snapshot.reference.collection(Constants.STUDENTS)
        } else {
            query = snapshot.reference.collection(Constants.STUDENTS).whereEqualTo(CURRENT_CLASS_GRADE, currentInfo.currentClassGrade)
        }
        query.get().addOnSuccessListener {
            offer(Resource.success(it.documents))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose {

        }
    }

    fun onCheckBoxClicked(snapshot: DocumentSnapshot, present: Boolean) = callbackFlow<Resource<DocumentSnapshot>> {

        val map = mapOf<String, Boolean>(PRESENT to present)
        snapshot.reference.set(map, SetOptions.merge()).addOnSuccessListener {
            offer(Resource.success(snapshot))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose { }
    }
}