package com.justice.schoolmanagement.presentation.ui.parent

import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.QuerySnapshot
import com.justice.schoolmanagement.presentation.ui.chat.util.FirestoreUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ParentRepository {

    suspend fun getParents(): Flow<Resource<QuerySnapshot>> = callbackFlow {

        val subscription = FirestoreUtil.getParents { snapshot, exception ->

            if (exception != null) {
                offer(Resource.error<Nothing>(exception))
            } else if (!snapshot!!.isEmpty()) {
                offer(Resource.success(snapshot))
            } else {
                offer(Resource.empty<Nothing>())

            }

        }
        awaitClose { subscription.remove() }
    }

}