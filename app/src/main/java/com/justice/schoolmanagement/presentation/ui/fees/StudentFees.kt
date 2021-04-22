package com.justice.schoolmanagement.presentation.ui.fees

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

const val STUDENT_FEES_ARGS = "studentFees"

@Parcelize
data class StudentFees(var payedAmount: Int = 0,
                       @ServerTimestamp var date: Date? = null) : Parcelable