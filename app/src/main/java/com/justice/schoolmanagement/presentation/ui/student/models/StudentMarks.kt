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
        var math: String = "0",
        var science: String = "0",
        var english: String = "0",
        var kiswahili: String = "0",
        var sst_cre: String = "0",
        var totalMarks: Int = 0
) : Parcelable, Comparable<StudentMarks> {
    override fun compareTo(other: StudentMarks): Int {
        return this.position.compareTo(other.position)
    }
}
