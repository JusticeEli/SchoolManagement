package com.justice.schoolmanagement.presentation.ui.blog.model

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Blog(
        var userId: String = "",
        @DocumentId
        var id: String? = null,
        var description: String = "",
        var photo: String = "",
        @ServerTimestamp
        var date: Date? = null,
        @Exclude
        var uri: Uri? = null
) : Parcelable