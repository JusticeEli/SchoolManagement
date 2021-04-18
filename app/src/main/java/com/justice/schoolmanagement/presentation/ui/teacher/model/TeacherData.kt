package com.justice.schoolmanagement.presentation.ui.teacher.model

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TeacherData(
        @DocumentId
        var id: String? = null,
        var fullName: String = "",
        var firstName: String = "",
        var lastName: String = "",
        var email: String = "",
        var salary: String = "",
        var city: String = "",
        var degree: String = "",
        var age: String = "",
        var gender: String = "",
        var photo: String = "",
        var subject: String = "",
        var contact: String = "",
        var type: String = "teacher",
        var thumbnail: String = "",
        var uri: Uri? = null,
        var registrationTokens: MutableList<String>? = null
) : Parcelable