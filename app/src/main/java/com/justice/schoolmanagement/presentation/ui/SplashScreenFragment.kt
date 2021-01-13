package com.justice.schoolmanagement.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentSplashScreenBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty
import java.util.*

class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {
    companion object {
        private const val TAG = "SplashScreenFragment"
        private const val RC_SIGN_IN = 4

    }

    lateinit var progressBar: ProgressBar
    lateinit var event: ListenerRegistration
    private val firebaseAuth = FirebaseAuth.getInstance()
    lateinit var binding: FragmentSplashScreenBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSplashScreenBinding.bind(view)
        checkIfUserIsLoggedIn()
        initProgressBar()
    }

    private fun initProgressBar() {
        progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        binding.relativeLayout.addView(progressBar, params)
        progressBar.isVisible = true
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
                    RC_SIGN_IN)
        } else {
            Log.d(TAG, "checkIfUserIsLoggedIn: user already logged in")

        }
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

    private fun goToDashBoard() {

        findNavController().navigate(R.id.action_splashScreenFragment_to_dashboardFragment)

    }

    private fun showToast(message: String) {
        Toasty.error(requireContext(), message).show()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: called")
        //if user is not logged in we want to exit this method
        if (firebaseAuth.currentUser == null) {
            Log.d(TAG, "onStart: user not signed in")


        } else {
            Log.d(TAG, "onStart: user signed in")

            val collectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_TEACHERS)
            event = collectionReference.document(FirebaseAuth.getInstance().currentUser!!.uid).addSnapshotListener(EventListener { documentSnapshot, e ->
                if (e != null) {
                    Log.d(TAG, "onEvent: Error: " + e.message)
                    Toast.makeText(activity, "Error: " + e.message, Toast.LENGTH_SHORT).show()
                    return@EventListener
                }
                if (!documentSnapshot!!.exists()) {
                    //teacher metadata does not exit
                    Log.d(TAG, "onEvent: teacher metadata does not exit going to AddTeacherActivity")


                    findNavController().navigate(R.id.action_splashScreenFragment_to_addTeacherFragment)


                } else {
                    //teacher has metadata
                    goToDashBoard()
                    //start loading current teacher names
                    (requireContext().applicationContext as ApplicationClass).loadTeacherNames()
                    if (documentSnapshot.getString("type") == "teacher") {
                        //its a teacher not admin
                        Log.d(TAG, "onEvent: teacher is not admin")
                        Constants.isAdmin = false
                    }
                }
            })

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        event.remove()

    }

}