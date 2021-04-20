package com.justice.schoolmanagement.presentation.splash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.edward.nyansapo.wrappers.Resource
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ActivitySplashScreenBinding
import com.justice.schoolmanagement.presentation.MainActivity
import com.justice.schoolmanagement.presentation.ui.admin.AdminFragment
import com.justice.schoolmanagement.presentation.ui.teacher.AddTeacherFragment
import com.justice.schoolmanagement.presentation.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import java.util.*

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    private val TAG = "SplashScreenActivity"

    companion object {
        private const val RC_SIGN_IN = 4
        const val SHARED_PREF = "shared_pref"
        val KEY_ADMIN_DATA = "admin_data"

    }

    lateinit var sharedPref: SharedPreferences

    lateinit var progressBar: ProgressBar
    private val viewModel: SplashScreenViewModel by viewModels()
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initProgressBar()
        sharedPref = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        subScribeToObservers()
        viewModel.setEvent(Event.CheckUserIsLoggedIn)


    }

    private fun subScribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.checkIsUserLoggedInStatus.collect {
                Log.d(TAG, "subScribeToObservers: checkIsUserLoggedInStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        viewModel.setEvent(Event.CheckIfInstitutionCodeExists)
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        startLogginProcess()
                    }
                }

            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.checkIfInstitutionCodeExistsStatus.collect {
                Log.d(TAG, "subScribeToObservers: checkIfInstitutionCodeExistsStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {

                    }
                    Resource.Status.SUCCESS -> {
                        Constants.DOCUMENT_CODE = it.data!!.institutionCode
                        viewModel.setEvent(Event.CheckIfUserIsSetup)

                    }
                    Resource.Status.ERROR -> {
                        viewModel.setEvent(Event.GoToAdminScreen)

                    }

                }
            }
        }
        lifecycleScope.launchWhenResumed {
            viewModel.checkIfUserIsSetupStatus.collect {
                Log.d(TAG, "subScribeToObservers: checkIfUserIsSetup:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)

                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        viewModel.setEvent(Event.GoToDashBoard)

                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        viewModel.setEvent(Event.GoToSetupScreen)

                    }

                }
            }
        }
        lifecycleScope.launchWhenResumed {
            viewModel.splashScreenEvents.collect {
                when (it) {
                    is Event.GoToAdminScreen -> {
                        goToAdminScreen()
                    }
                    is Event.GoToSetupScreen -> {
                        goToSetupScreen()
                    }
                    is Event.GoToDashBoard -> {
                        goToDashBoardScreen()
                    }


                }
            }
        }
    }

    private fun goToAdminScreen() {
        supportFragmentManager.beginTransaction().replace(R.id.container, AdminFragment()).commit()
    }

    private fun goToSetupScreen() {
        supportFragmentManager.beginTransaction().replace(R.id.container, AddTeacherFragment()).commit()
    }

    private fun goToDashBoardScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startLogginProcess() {
        Log.d(TAG, "startLogginProcess: ")
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()

                        .setAvailableProviders(Arrays.asList(
                                AuthUI.IdpConfig.GoogleBuilder().build(),
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                AuthUI.IdpConfig.PhoneBuilder().build(),
                                AuthUI.IdpConfig.AnonymousBuilder().build()))
                        .build(),
                RC_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                //is sign in is success we want to recreate the activity
                Log.d(TAG, "onActivityResult: success sign in")
                viewModel.setEvent(Event.CheckIfInstitutionCodeExists)

            } else {
                // Sign in failed
                Log.d(TAG, "onActivityResult: sign in failed")
                if (response == null) {
                    // User pressed back button
                    showToast("sign in cancelled")
                    return
                }
                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    showToast("not internet connection")
                    return
                }
                showToast("unknown error")
                Log.e(TAG, "Sign-in error: ", response.error)
            }
        }
    }

    private fun showProgress(progress: Boolean) {
        progressBar.isVisible = progress
    }

    private fun showToast(message: String) {
        Toasty.info(this, message).show()
    }

    private fun initProgressBar() {
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        binding.relativeLayout.addView(progressBar, params)
        progressBar.isVisible = true

    }


    sealed class Event {
        object CheckIfInstitutionCodeExists : Event()
        object CheckUserIsLoggedIn : Event()
        object CheckIfUserIsSetup : Event()
        object GoToDashBoard : Event()
        object GoToSetupScreen : Event()
        object GoToAdminScreen : Event()
    }

}

var SharedPreferences.adminData
    get() = this.getString(SplashScreenActivity.KEY_ADMIN_DATA, null)
    set(value) = this.edit().putString(SplashScreenActivity.KEY_ADMIN_DATA, value).apply()
