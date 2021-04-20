package com.justice.schoolmanagement.presentation.ui.teacher

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.example.edward.nyansapo.wrappers.Resource
import com.firebase.ui.auth.AuthUI
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentAddTeacherBinding
import com.justice.schoolmanagement.presentation.splash.SplashScreenActivity
import com.justice.schoolmanagement.presentation.splash.adminData
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_add_parent.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class AddTeacherFragment : Fragment(R.layout.fragment_add_teacher) {

    private val TAG = "AddTeacherFragment"

    @Inject
    lateinit var requestManager: RequestManager
    private var uri: Uri? = null
    lateinit var progressBar: ProgressBar
    lateinit var binding: FragmentAddTeacherBinding
    private val viewModel: AddTeacherViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding = FragmentAddTeacherBinding.bind(view)
        initProgressBar()
        setDefaultValues()
        setOnClickListeners()
        setUpSubjectsSpinner()
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.addTeacherStatus.collect {
                Log.d(TAG, "subScribeToObservers: addTeacherStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)

                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        showToastInfo("Success Saving data")

                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToastInfo("Error: ${it.exception?.message}")

                    }
                }
            }
        }
    }


    private fun setUpSubjectsSpinner() {
        val subjects = requireActivity().resources.getStringArray(R.array.subjects)
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects)
        binding.subjectSpinner.setAdapter(arrayAdapter)


    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.logout_menu, menu)
        return super.onCreateOptionsMenu(menu, inflater)

    }

    private fun setDefaultValues() {
        binding.contactEdtTxt.setText("07")
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: item selected")
        if (item.itemId == R.id.logoutMenu) {
            Log.d(TAG, "onOptionsItemSelected: logout pressed")
            logout()
        }
        return true

    }

    private fun logout() {
        AuthUI.getInstance().signOut(requireContext()).addOnSuccessListener {

            Log.d(TAG, "onNavigationItemSelected: logout success")
            getRidOfSharedPreferenceData()
            requireActivity().finish()
        }
    }

    private fun getRidOfSharedPreferenceData() {
        val sharedPreferences = requireActivity().getSharedPreferences(SplashScreenActivity.SHARED_PREF, Context.MODE_PRIVATE)
        sharedPreferences.adminData = null

    }

    private fun showToastInfo(message: String) {
        Toasty.error(requireContext(), message).show()
    }

    private fun setOnClickListeners() {

        binding.apply {

            addPhotoBtn.setOnClickListener { choosePhoto() }
            imageView.setOnClickListener { choosePhoto() }
            binding.submitBtn.setOnClickListener {

                if (uri == null) {
                    showToastInfo("Please choose a photo")
                    return@setOnClickListener
                }
                val teacher = getTeacherObject()
                viewModel.setEvent(Event.TeacherAddSubmitClicked(teacher))


            }



            contactEdtTxt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (contactEdtTxt.length() == 10) {
                        salaryEdtTxt.requestFocus()
                    }
                    if (contactEdtTxt.length() > 10) {
                        contactEdtTxt.setError("Contact Must have 10 characters")
                    }
                }

                override fun afterTextChanged(s: Editable) {}
            })


        }

    }

    private fun getTeacherObject(): TeacherData {

        val teacherData = TeacherData()

        binding.apply {
            teacherData.firstName = firstNameEdtTxt.text.toString()
            teacherData.lastName = lastNameEdtTxt.text.toString()
            teacherData.fullName = "${teacherData.firstName} ${teacherData.lastName}"
            teacherData.email = emailEdtTxt.text.toString()
            teacherData.salary = salaryEdtTxt.text.toString()
            teacherData.city = cityEdtTxt.text.toString()
            teacherData.degree = degreeEdtTxt.text.toString()
            teacherData.age = ageEdtTxt.text.toString()
            teacherData.contact = contactEdtTxt.text.toString()
            teacherData.gender = getSelectedGenderRadioBtn()
            teacherData.subject = subjectSpinner.selectedItem.toString()

            teacherData.uri = uri
        }
        return teacherData

    }

    private fun choosePhoto() {
        Log.d(TAG, "choosePhoto: ")
        // start picker to get image for cropping and then use the image in cropping activity
        /*  CropImage.activity()
                  .setGuidelines(CropImageView.Guidelines.ON)
                  .setAspectRatio(1, 1)
                  .start(requireActivity())
  */

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(requireContext(), this);
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult:  callback")
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                uri = result.uri
                Log.d(TAG, "onActivityResult: image received" + uri)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }

        requestManager.load(uri).into(binding.imageView)
    }

    private fun resetEdtTxt() {
        binding.apply {

            firstNameEdtTxt.setText("")
            lastNameEdtTxt.setText("")
            salaryEdtTxt.setText("")
            cityEdtTxt.setText("")
            degreeEdtTxt.setText("")
            ageEdtTxt.setText("")
            contactEdtTxt.setText("")
        }

    }


    private fun getSelectedGenderRadioBtn(): String {
        when (binding.genderRadioGroup.getCheckedRadioButtonId()) {
            R.id.maleRadioBtn -> return "Male"
            R.id.femaleRadioBtn -> return "Female"
            else -> return "Other"
        }

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
        data class TeacherAddSubmitClicked(val teacher: TeacherData) : Event()
    }
}