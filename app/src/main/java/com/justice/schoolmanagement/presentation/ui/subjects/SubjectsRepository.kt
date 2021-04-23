package com.justice.schoolmanagement.presentation.ui.subjects

import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class SubjectsRepository {
    fun getAllTeachers() = callbackFlow<Resource<List<DocumentSnapshot>>> {
        offer(Resource.loading(""))
        FirebaseUtil.collectionReferenceTeachers().get().addOnSuccessListener {
            offer(Resource.success(it.documents))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose {

        }
    }
}