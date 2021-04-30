package com.justice.schoolmanagement.presentation.ui.results

import android.util.Log
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import com.justice.schoolmanagement.presentation.ui.student.models.CLASS_GRADE
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.presentation.ui.student.models.TOTAL_MARKS
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class ResultsRepository {

    private val TAG = "ResultsRepository"

    fun getAllMarks() = callbackFlow<Resource<List<DocumentSnapshot>>> {

        try {
            offer(Resource.loading(""))
            FirebaseUtil.collectionReferenceStudentsMarks().orderBy(TOTAL_MARKS, Query.Direction.DESCENDING).addSnapshotListener { value, error ->
                if (error != null) {
                    offer(Resource.error(error))
                } else if (value!!.isEmpty) {
                    offer(Resource.empty())
                } else {
                    offer(Resource.success(value!!.documents))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        awaitClose {

        }
    }

    fun getAllMarksByClass(classGrade: String) = callbackFlow<Resource<List<DocumentSnapshot>>> {
        try {
            offer(Resource.loading(""))
            FirebaseUtil.collectionReferenceStudentsMarks().whereEqualTo(CLASS_GRADE, classGrade).orderBy(TOTAL_MARKS, Query.Direction.DESCENDING).addSnapshotListener { value, error ->
                if (error != null) {
                    offer(Resource.error(error))
                } else if (value!!.isEmpty) {
                    offer(Resource.empty())
                } else {
                    offer(Resource.success(value!!.documents))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }



        awaitClose {

        }
    }

    fun getStudentMarks(id: String) = callbackFlow<Resource<DocumentSnapshot>> {


        FirebaseUtil.collectionReferenceStudentsMarks().document(id).get().addOnSuccessListener {
            Log.d(TAG, "getStudentMarks: success")
            try {
                offer(Resource.success(it))
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            Log.d(TAG, "getStudentMarks: end")
        }.addOnFailureListener {
            Log.d(TAG, "getStudentMarks: failed")
            offer(Resource.error(it))
        }


        awaitClose {

        }
    }

    fun updateDatabase(snapshot: DocumentSnapshot, map: Map<String,String>) = callbackFlow<Resource<DocumentSnapshot>> {
        Log.d(TAG, "updateDatabase: ")

        snapshot.reference.update(map).addOnSuccessListener {
            Log.d(TAG, "updateDatabase: start success")
            offer(Resource.success(snapshot))
            Log.d(TAG, "updateDatabase: success")
        }.addOnFailureListener {
            offer(Resource.error(it))
        }



        awaitClose {

        }
    }

    fun updateDatabase2(snapshot: DocumentSnapshot, studentMarks: StudentMarks) = callbackFlow<Resource<DocumentSnapshot>> {
        Log.d(TAG, "updateDatabase: ")
        try {
            offer(Resource.loading(""))
          //  snapshot.reference.set(studentMarks).await()
            Log.d(TAG, "updateDatabase2: end")
        } catch (e: Throwable) {
            e.printStackTrace()
        }



        awaitClose {

        }
    }


}