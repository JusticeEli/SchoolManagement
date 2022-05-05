package com.justice.schoolmanagement.presentation.ui.register

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class CurrentDate(@ServerTimestamp val date:Date?=null) {
}