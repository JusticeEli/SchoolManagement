package com.justice.schoolmanagement.presentation.ui.admin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AdminData (var name:String="",var phone:String="",var institutionCode:String=""):Parcelable{
}