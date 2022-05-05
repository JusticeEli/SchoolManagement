package com.justice.schoolmanagement.presentation.ui.parent.model

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ParentData(
        @DocumentId
        var id: String? = null,
        var firstName: String = "",
        var lastName: String = "",
        var fullName: String = "",
        var contact: String = "",
        var city: String = "",
        var jobStatus: String = "",
        var age: String = "",
        var gender: String = "",
        var jobType: String = "",
        var email: String = "",
        var photo: String = "",
        var thumbnail: String = "",
        @Exclude
        var uri: Uri? = null
) : Parcelable