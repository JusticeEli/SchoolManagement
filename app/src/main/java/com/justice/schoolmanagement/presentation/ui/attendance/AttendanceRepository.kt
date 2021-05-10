package com.justice.schoolmanagement.presentation.ui.attendance

import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.attendance.model.CurrentPosition
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import com.justice.schoolmanagement.utils.formatDate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

class AttendanceRepository {
    suspend fun getCurrentDate(): Date {
        var currentDate: Date? = FirebaseUtil.getCurrentDate2()
        if (FirebaseUtil.getCurrentDate2() == null) {
            return Calendar.getInstance().time
        } else {
            return currentDate!!
        }
    }

    fun getCurrentDate2() = callbackFlow<Resource<String>> {
        offer(Resource.loading(""))
        FirebaseUtil.getCurrentDate {
            var currentDate: String
            if (it == null) {
                currentDate = Calendar.getInstance().time.formatDate
            } else {
                currentDate = it.formatDate

            }

            offer(Resource.success(currentDate))


        }


        awaitClose { }
    }

    fun startFetchingAttendance(choosenDate: String) = callbackFlow<Resource<List<DocumentSnapshot>>> {
        offer(Resource.loading(""))
        FirebaseUtil.documentReferenceCurrentLocation().collection(choosenDate).addSnapshotListener { value, error ->

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

    fun uploadCurrentPosition(currentPosition: CurrentPosition) = callbackFlow<Resource<CurrentPosition>> {
        offer(Resource.loading(""))
        FirebaseUtil.documentReferenceCurrentLocation().set(currentPosition).addOnSuccessListener {
            offer(Resource.success(currentPosition))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose {

        }
    }
}