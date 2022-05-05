package com.justice.schoolmanagement.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.justice.schoolmanagement.NavGraphDirections
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ActivityMainBinding
import com.justice.schoolmanagement.presentation.ui.splash.SplashScreenActivity.Companion.SHARED_PREF
import com.justice.schoolmanagement.presentation.ui.splash.adminData
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.ui.video_chat.VideoChatViewActivity
import com.justice.schoolmanagement.utils.FirebaseUtil
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        val set = setOf(R.id.dashboardFragment, R.id.teachersFragment, R.id.studentsFragment, R.id.parentsFragment, R.id.classesFragment, R.id.subjectsFragment)

        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.dashboardFragment, R.id.studentsFragment, R.id.splashScreenFragment),
                drawer_layout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)
        bottom_nav.setupWithNavController(navController)

        /*nav_view.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                if (item.itemId == R.id.logoutMenu) {
                    AuthUI.getInstance().signOut(this@MainActivity).addOnSuccessListener {

                        Log.d(Companion.TAG, "onNavigationItemSelected: logout success")
                        //TODO user logout out
                    }
                }

                return true
            }
        })
*/

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.d(TAG, "onOptionsItemSelected: ")
        return when (item.itemId) {
            R.id.blogsMenu -> {

                navController.navigate(R.id.blogFragment)
                return true
            }
            R.id.logoutMenu -> {
                logout()
                return true
            }
            R.id.myAccountMenu -> {
                showMyAccountDetails()
                return true
            }
            R.id.checkInCheckOutFragment -> {
                navController.navigate(R.id.checkInCheckOutFragment)
                return true
            }
            R.id.attendanceFragment -> {
                navController.navigate(R.id.attendanceFragment)
                return true
            }
            R.id.setLocationFragment -> {
                navController.navigate(R.id.setLocationFragment)
                return true
            }
            R.id.markExamFragment -> {
               Toasty.info(this,"Not Yet implememented")
                return true
            }

            R.id.videoChatMenu->{
                startActivity(Intent(this,VideoChatViewActivity::class.java))
                return true
            }
            else -> {
                item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)

            }
        }


    }

    private fun showMyAccountDetails() {


        FirebaseUtil.getCurrentUser {

            navController.navigate(NavGraphDirections.actionGlobalTeacherDetailsFragment(it!!.toObject(TeacherData::class.java)!!))

        }
    }

    private fun logout() {
        AuthUI.getInstance().signOut(this@MainActivity).addOnSuccessListener {

            Log.d(Companion.TAG, "onNavigationItemSelected: logout success")

            getRidOfSharedPreferenceData()

            finish()
        }
    }

    private fun getRidOfSharedPreferenceData() {
        val sharedPreferences = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        sharedPreferences.adminData=null

    }

    /*    override fun onSupportNavigateUp(): Boolean {


           return navigateUp(navController, drawer_layout)
       }*/

    override fun onSupportNavigateUp(): Boolean {

        return navigateUp(navController, drawer_layout)

    }


/*
    override fun onSupportNavigateUp(): Boolean {


        return when(navController.currentDestination?.id) {
            R.id.splashScreenFragment -> {
                // custom behavior here
                false
            }
            else -> !navigateUp(navController, drawer_layout)
        }
    }
*/


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
    // This might just fix your issue by itself.



    companion object {
        private const val TAG = "MainActivity"
    }


}