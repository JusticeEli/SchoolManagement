package com.justice.schoolmanagement.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.presentation.ui.dashboard.MainFragment
import es.dmoral.toasty.Toasty
import java.util.*

class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {
    companion object {
        private const val TAG = "SplashScreenFragment"
    }

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkIfUserIsLoggedIn()
    }


    private fun checkIfUserIsLoggedIn() {
        Log.d(TAG, "checkIfUserIsLoggedIn: checking if user is logged in")
        if (firebaseAuth.currentUser == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    AuthUI.IdpConfig.GoogleBuilder().build(),
                                    AuthUI.IdpConfig.EmailBuilder().build(),
                                    AuthUI.IdpConfig.PhoneBuilder().build(),
                                    AuthUI.IdpConfig.AnonymousBuilder().build()))
                            .build(),
                    MainFragment.RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == MainFragment.RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                //is sign in is success we want to recreate the activity
                Log.d(TAG, "onActivityResult: success sign in")
                Log.d(TAG, "onActivityResult: recreating the activity")
                //   recreateFragment()
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

    private fun showToast(message: String) {
        Toasty.error(requireContext(), message).show()
    }
}