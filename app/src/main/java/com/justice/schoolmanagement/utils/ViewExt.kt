package com.justice.schoolmanagement.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import com.firebase.ui.auth.AuthUI
import com.justice.schoolmanagement.NavGraphDirections
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.presentation.MainActivity
import com.justice.schoolmanagement.presentation.ui.splash.SplashScreenActivity
import com.justice.schoolmanagement.presentation.ui.splash.adminData
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.ui.video_chat.VideoChatViewActivity
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}


val <T> T.exhaustive: T
    get() = this




val Date.formatDate get() = SimpleDateFormat("dd/MM/yyyy").format(this)

val String.cleanString
    get() =
        this.replace("/", "_")


fun String.toIntOrZero():Int{
    try {
        return   this.toInt()
    }catch (e:Exception)
    {
        Log.e("ViewExt", "toIntOrZero: ", e)
    }

    return 0
}



 fun MainActivity.onOptionsItemSelected(item: MenuItem): Boolean {


    return when (item.itemId) {
        R.id.blogsMenu -> {

            this.navController.navigate(R.id.blogFragment)
            return true
        }
        R.id.logoutMenu -> {
            AuthUI.getInstance().signOut(this).addOnSuccessListener {


                val sharedPreferences = getSharedPreferences(SplashScreenActivity.SHARED_PREF, Context.MODE_PRIVATE)
                sharedPreferences.adminData=null

                finish()
            }
            return true
        }
        R.id.myAccountMenu -> {
            FirebaseUtil.getCurrentUser {

                navController.navigate(
                    NavGraphDirections.actionGlobalTeacherDetailsFragment(it!!.toObject(
                        TeacherData::class.java)!!))

            }
            return true
        }
        R.id.checkInCheckOutFragment -> {
            this.navController.navigate(R.id.checkInCheckOutFragment)
            return true
        }
        R.id.attendanceFragment -> {
            this.navController.navigate(R.id.attendanceFragment)
            return true
        }
        R.id.setLocationFragment -> {
            this.navController.navigate(R.id.setLocationFragment)
            return true
        }
        R.id.markExamFragment -> {
            Toasty.info(this,"Not Yet implememented")
            return true
        }

        R.id.videoChatMenu->{
            startActivity(Intent(this, VideoChatViewActivity::class.java))
            return true
        }
        else -> {
        false

        }
    }



}
