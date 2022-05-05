package com.justice.schoolmanagement.presentation.ui.attendance.model

data class CurrentPosition(var latitude:Double,var longitude:Double,var radius:Int){

    constructor():this(0.0,0.0,0)
}
