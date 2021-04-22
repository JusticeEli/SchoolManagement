package com.justice.schoolmanagement.presentation.ui.register

import java.util.*

const val CURRENT_CLASS_GRADE="currentClassGrade"
data class CurrentInfo(var currentDateString:String="", var currentClassGrade:String="all", var currentTab:Int=0, var dateChoosen: Date?=null) {
}