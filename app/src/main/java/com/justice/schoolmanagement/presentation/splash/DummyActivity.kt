package com.justice.schoolmanagement.presentation.splash


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.justice.schoolmanagement.R

class DummyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummy)
    }

    fun clicked(view: View) {
        val intent = Intent(this, SplashScreenActivity::class.java)
        startActivity(intent)
    }
}