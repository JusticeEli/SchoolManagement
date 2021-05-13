package com.justice.schoolmanagement.presentation.ui.register

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.utils.Constants
import com.justice.schoolmanagement.utils.FirebaseUtil
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*


class RegisterRepository {

    private val TAG = "RegisterRepository"

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

    suspend fun getCurrentDate2(): Date {
        val date = FirebaseUtil.getCurrentDate2()
        if (date == null) {
            return Calendar.getInstance().time
        } else {
            return date
        }

    }

    fun startFetchingData(currentInfo: CurrentInfo) = callbackFlow<Resource<DocumentSnapshot>> {

        FirebaseUtil.collectionReferenceDate().document(currentInfo.currentDateString).get().addOnSuccessListener {
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
        if (currentInfo.currentClassGrade.equals("all")) {
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

    fun documentDoesNotExist(currentInfo: CurrentInfo) = callbackFlow<Resource<List<DocumentSnapshot>>> {

        FirebaseUtil.collectionReferenceDate().document(currentInfo.currentDateString).set(currentInfo).await()
        FirebaseUtil.collectionReferenceStudents().get().await().forEach { queryDocumentSnapshot ->
            val studentData = queryDocumentSnapshot.toObject(StudentData::class.java)
            val studentRegistrationData = StudentRegistrationData(queryDocumentSnapshot.id, true, studentData.classGrade.toString(), studentData)
            FirebaseUtil.collectionReferenceDate().document(currentInfo.currentDateString).collection(Constants.STUDENTS).add(studentRegistrationData).await()
        }

        FirebaseUtil.collectionReferenceDate().document(currentInfo.currentDateString).collection(Constants.STUDENTS).get().addOnSuccessListener {

        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose { }
    }


    fun documentDoesNotExist2(currentInfo: CurrentInfo) = callbackFlow<Resource<List<DocumentSnapshot>>> {

        FirebaseUtil.collectionReferenceStudents().get().addOnSuccessListener {
            it?.forEach { queryDocumentSnapshot ->

                val studentData = queryDocumentSnapshot.toObject(StudentData::class.java)

                val studentRegistrationData = StudentRegistrationData(queryDocumentSnapshot.id, true, studentData.classGrade.toString(), studentData)

                FirebaseUtil.collectionReferenceDate().document(currentInfo.currentDateString).collection(Constants.STUDENTS).add(studentRegistrationData)
                        .addOnSuccessListener {
                            Log.d(TAG, "documentDoesNotExist: success adding student")
                        }.addOnFailureListener {
                            Log.d(TAG, "documentDoesNotExist: failed adding student")
                        }


            }

        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose { }
    }
}