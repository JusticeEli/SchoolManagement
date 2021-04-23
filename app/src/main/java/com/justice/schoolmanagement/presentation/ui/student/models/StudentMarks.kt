package com.justice.schoolmanagement.presentation.ui.student.models


import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.android.parcel.Parcelize

const val TOTAL_MARKS="totalMarks"
const val STUDENT_MARKS_ARGS="studentMarks"
@Parcelize
data class StudentMarks(
        @DocumentId
        var id: String? = null,
        var position: Int = 0,
        var fullName: String = "",
        var email: String = "",
        var classGrade: String = "",
        var math: String = "",
        var science: String = "",
        var english: String = "",
        var kiswahili: String = "",
        var sst_cre: String = "",
        var totalMarks: String = ""
) : Parcelable, Comparable<StudentMarks> {
    override fun compareTo(other: StudentMarks): Int {
        return this.position.compareTo(other.position)
    }
}