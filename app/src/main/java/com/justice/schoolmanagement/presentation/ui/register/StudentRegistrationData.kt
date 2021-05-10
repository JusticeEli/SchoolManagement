package com.justice.schoolmanagement.presentation.ui.register

import com.justice.schoolmanagement.presentation.ui.student.models.StudentData


const val PRESENT="present"

data class StudentRegistrationData(val studentId:String,var present:Boolean=true,val currentClass:String,val studentData: StudentData?) {
    constructor():this("",true,"",null)

}