package com.justice.schoolmanagement.presentation.ui.student.models

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.android.parcel.Parcelize

const val CLASS_GRADE="classGrade"
@Parcelize
data class StudentData(
        @DocumentId
        var id: String? = null,
        var fullName: String = "",
        var classGrade: Int = 0,
        var firstName: String = "",
        var lastName: String = "",
        var nationality: String = "",
        var religion: String = "",
        var email: String = "",
        var parentName: String = "",
        var dateOfBirth: String = "",
        var dateOfArrival: String = "",
        var age: String = "",
        var gender: String = "",
        var classTeacherName: String = "",
        var city: String = "",
        var photo: String = "",
        var photoName: String = "",
        var thumbnail: String = "",
        var totalFees: Int = 0,
        var uri: Uri? = null
):Parcelable