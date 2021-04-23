package com.justice.schoolmanagement.presentation.ui.results

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
    fun getAllMarks() = callbackFlow<Resource<List<DocumentSnapshot>>> {
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

        awaitClose {

        }
    }    fun getAllMarksByClass(classGrade:String) = callbackFlow<Resource<List<DocumentSnapshot>>> {
        offer(Resource.loading(""))
        FirebaseUtil.collectionReferenceStudentsMarks().whereEqualTo(CLASS_GRADE,classGrade).orderBy(TOTAL_MARKS, Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null) {
                offer(Resource.error(error))
            } else if (value!!.isEmpty) {
                offer(Resource.empty())
            } else {
                offer(Resource.success(value!!.documents))
            }
        }

        awaitClose {

        }
    }

    fun getStudentMarks(id: String) = callbackFlow<Resource<DocumentSnapshot>> {
        offer(Resource.loading(""))
        FirebaseUtil.collectionReferenceStudentsMarks().document(id).get().addOnSuccessListener {
            offer(Resource.success(it))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }
        awaitClose {

        }
    }

    fun updateDatabase(snapshot: DocumentSnapshot, studentMarks: StudentMarks) = callbackFlow<Resource<StudentMarks>> {
        offer(Resource.loading(""))
        snapshot.reference.set(studentMarks).addOnSuccessListener {
            offer(Resource.success(studentMarks))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose { }
    }


}