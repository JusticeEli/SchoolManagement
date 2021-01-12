package com.justice.schoolmanagement.presentation

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.justice.schoolmanagement.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.dashboardFragment, R.id.teachersFragment, R.id.studentsFragment, R.id.parentsFragment, R.id.classesFragment, R.id.subjectsFragment, R.id.resultsFragment),
                drawer_layout
        )
        setSupportActionBar(toolbar)

        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)

        nav_view.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener {
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


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        /*       menuInflater.inflate(R.menu.options_menu, menu)
        */       return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /*     return if (item.itemId == R.id.termsAndConditions) {
                  true
             } else {
                 item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
             }*/
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}