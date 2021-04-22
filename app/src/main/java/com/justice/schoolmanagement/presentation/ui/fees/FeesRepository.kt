package com.justice.schoolmanagement.presentation.ui.fees

import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import com.justice.schoolmanagement.presentation.utils.Constants
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
}