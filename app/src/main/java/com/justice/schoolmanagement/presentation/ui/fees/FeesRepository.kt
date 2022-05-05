package com.justice.schoolmanagement.presentation.ui.fees

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.justice.schoolmanagement.utils.Constants
import com.justice.schoolmanagement.utils.FirebaseUtil
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FeesRepository {
    fun getAllFees(id: String) = callbackFlow<Resource<List<DocumentSnapshot>>> {
        offer(Resource.loading(""))
        FirebaseUtil.collectionReferenceStudents().document(id).get().await()
                .reference.collection(Constants.COLLECTION_FEES).orderBy(DATE).addSnapshotListener { value, error ->
                    if (error != null) {
                        offer(Resource.error(error))
                    } else if (value!!.isEmpty) {
                        offer(Resource.empty())
                    } else {
                        offer(Resource.success(value.documents))
                    }

                }


        awaitClose { }
    }

    fun deleteFees(snapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {
        offer(Resource.loading(""))
        snapshot.reference.delete().addOnSuccessListener {
            offer(Resource.success(snapshot))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose {

        }
    }

    fun getStudent(id: String) = callbackFlow<Resource<DocumentSnapshot>> {
        FirebaseUtil.collectionReferenceStudents().document(id).get().addOnSuccessListener {
            offer(Resource.success(it))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }
        awaitClose {

        }
    }

    fun uploadFees(map: Map<String, Int>, snapshot: DocumentSnapshot) = callbackFlow<Resource<Map<String, Int>>> {
        offer(Resource.loading(""))
        snapshot.reference.set(map, SetOptions.merge()).addOnSuccessListener {
            offer(Resource.success(map))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose {

        }
    }

    fun startFetchingFees(studentId: String, feesId: String) = callbackFlow<Resource<DocumentSnapshot>> {
        offer(Resource.loading(""))
        FirebaseUtil.collectionReferenceStudents().document(studentId).get().await().reference.collection(Constants.COLLECTION_FEES).document(feesId).get().addOnSuccessListener {
            offer(Resource.success(it))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose { }
    }

    fun startUpdating(snapshot: DocumentSnapshot, studentFees: StudentFees) = callbackFlow<Resource<StudentFees>> {
        val map = mapOf("payedAmount" to studentFees.payedAmount)
        offer(Resource.loading(""))
        snapshot.reference.set(map, SetOptions.merge()).addOnSuccessListener {
            offer(Resource.success(studentFees))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }


        awaitClose { }
    }

    fun startAdding(snapshot: DocumentSnapshot, studentFees: StudentFees) = callbackFlow<Resource<StudentFees>> {
        offer(Resource.loading(""))
        snapshot.reference.collection(Constants.COLLECTION_FEES).add(studentFees).addOnSuccessListener {
            offer(Resource.success(studentFees))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose {

        }
    }
}