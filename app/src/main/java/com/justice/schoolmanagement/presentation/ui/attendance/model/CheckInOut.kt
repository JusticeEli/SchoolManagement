package com.justice.schoolmanagement.presentation.ui.attendance.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class CheckInOut(var image:String="", var fullName:String="", var checkIn: Boolean =false, @ServerTimestamp  var checkInTime: Date?=null, var checkOut:Boolean=false, @ServerTimestamp var checkOutTime:Date?=null) {



}