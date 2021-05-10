package com.justice.schoolmanagement.presentation.ui.admin

import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.*
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.gson.Gson
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentAdminBinding
import com.justice.schoolmanagement.presentation.MainActivity
import com.justice.schoolmanagement.presentation.splash.SplashScreenActivity.Companion.SHARED_PREF
import com.justice.schoolmanagement.presentation.splash.adminData
import com.justice.schoolmanagement.presentation.ui.teacher.AddTeacherFragment
import com.justice.schoolmanagement.presentation.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AdminFragment : Fragment(R.layout.fragment_admin) {

    private val TAG = "AdminFragment"

    lateinit var sharedPref: SharedPreferences
    lateinit var binding: FragmentAdminBinding
    private val viewModel: AdminViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminBinding.bind(view)
        sharedPref = requireContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE)
        initProgressBar()
        setOnClickListeners()
        subScribeToObservers()
        viewModel.setEvent(Event.CheckIfAdminDataExists)
    }

    private fun subScribeToObservers() {
        lifecycleScope.launchWhenResumed {
            viewModel.fetchDataFromSharedPrefStatus.collect {
                Log.d(TAG, "subScribeToObservers: adminDataStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {

                    }
                    Resource.Status.SUCCESS -> {
                        Constants.DOCUMENT_CODE = it.data!!.institutionCode
                        setDefaultValues(it.data!!)

                    }
                    Resource.Status.ERROR -> {

                    }

                }
            }
        }
        lifecycleScope.launchWhenResumed {
            viewModel.saveAdminDataStatus.collect {
                Log.d(TAG, "subScribeToObservers:saveAdminDataStatus:${it.status.name} ")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)

                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        Constants.DOCUMENT_CODE = it.data!!.institutionCode
                        adminDataExists(it.data!!)

                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)
                        //am creating a new institution
                        adminDataDoesNotExist()

                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.adminEvents.collect {
                when (it) {
                    is Event.GoToSetupScreen -> {
                        goToSetupScreen()
                    }
                    is Event.GoToDashBoard -> {
                        goToDashBoardScreen()
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
    }

    private fun adminDataDoesNotExist() {

        //set visibility of edit text on
        binding.apply {
            institutionCodeEdtTxt.isEnabled = false
            name.visibility = View.VISIBLE
            phone.visibility = View.VISIBLE
        }

        val adminData = getAdminObject()
        viewModel.setEvent(Event.SaveAdminDataInDB(adminData))
    }

    private fun adminDataExists(adminData: AdminData) {
        Log.d(TAG, "adminDataExists: adminData:$adminData")
        val stringData = Gson().toJson(adminData)
        sharedPref.adminData = stringData
        viewModel.setEvent(Event.CheckIfUserIsSetup)

    }

    private fun setOnClickListeners() {

        binding.submitBtn.setOnClickListener {
            val adminData = getAdminObject()
            viewModel.setEvent(Event.SubmitClicked(adminData))
        }
    }

    private fun getAdminObject(): AdminData {
        val adminData = AdminData()
        binding.apply {
            adminData.institutionCode = institutionCodeEdtTxt.text.toString()
            adminData.name = nameEdtTxt.text.toString()
            adminData.phone = phoneNumberEdtTxt.text.toString()
        }
        return adminData
    }

    private fun setDefaultValues(adminData: AdminData) {
        binding.nameEdtTxt.setText(adminData.name)
        binding.phoneNumberEdtTxt.setText(adminData.phone)
        binding.institutionCodeEdtTxt.setText(adminData.institutionCode)
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    lateinit var dialog: AlertDialog

    private fun showProgress(show: Boolean) {

        if (show) {
            dialog.show()

        } else {
            dialog.dismiss()

        }

    }

    private fun goToSetupScreen() {
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.container, AddTeacherFragment()).commit()
    }

    private fun goToDashBoardScreen() {
        Log.d(TAG, "goToDashBoardScreen: institution code:${Constants.DOCUMENT_CODE}")

        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun initProgressBar() {

        dialog = setProgressDialog(requireContext(), "Loading..")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    fun setProgressDialog(context: Context, message: String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    //end progressbar

    sealed class Event {
        data class SubmitClicked(val adminData: AdminData) : Event()
        data class SaveAdminDataInDB(val adminData: AdminData) : Event()
        object CheckIfAdminDataExists : Event()
        object CheckIfUserIsSetup : Event()
        object GoToDashBoard : Event()
        object GoToSetupScreen : Event()


    }

}