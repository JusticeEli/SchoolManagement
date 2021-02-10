package com.justice.schoolmanagement.presentation.ui.fees

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class StudentFees(var payedAmount: Int, @ServerTimestamp var date: Date? = null){

    constructor() : this(0,null)
}