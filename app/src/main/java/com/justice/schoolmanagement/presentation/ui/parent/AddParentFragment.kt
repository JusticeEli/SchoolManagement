package com.justice.schoolmanagement.presentation.ui.parent

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
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentAddParentBinding
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import com.justice.schoolmanagement.utils.Resource
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_add_parent.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddParentFragment : Fragment(R.layout.fragment_add_parent) {

    private val TAG = "AddParentFragment"


    lateinit var progressBar: ProgressBar
    private var uri: Uri? = null
    lateinit var binding: FragmentAddParentBinding
    val navArgs: AddParentFragmentArgs by navArgs()
    private val viewModel: AddParentViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddParentBinding.bind(view)
        initProgressBar()
        setUpViews()
        setSkipBtn()
        initSpinnerAdapters()
        setOnClickListeners()
        subsribeToObservers()

    }

    private fun subsribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.addParentStatus.collect {
                //   Log.d(TAG, "subsribeToObservers: addParentStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                        Log.d(TAG, "subsribeToObservers: LOADING:${it.message}")
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        Log.d(TAG, "subsribeToObservers: Error:${it.exception?.message}")
                    }
                    Resource.Status.EMPTY -> {
                        showToastError("Please Fill All Fields")
                    }

                }
            }
        }
    }

    private fun setUpViews() {
        binding.contactEdtTxt.setText("07")
    }


    private fun choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(requireContext(), this);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                uri = result.uri
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(binding.imageView)
    }

    //if executed when we are adding student then we need to add a parent
    private fun setSkipBtn() {
        val student = navArgs.student
        if (student != null) {
            binding.apply {
                skipLinearLayout.setVisibility(View.VISIBLE)
                firstNameEdtTxt.setText(student.parentName)
                emailEdtTxt.setText(student.email)

            }
        }
    }

    private fun initSpinnerAdapters() {
        //   val jobStatus = arrayOf("Employed", "Unemployed", "Retired")
        val jobStatus = requireActivity().resources.getStringArray(R.array.job_status)
        val jobStatusAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, jobStatus)
        binding.jobStatusSpinner.setAdapter(jobStatusAdapter)


        val cities = requireActivity().resources.getStringArray(R.array.cities)
        val cityAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, cities)
        binding.cityEdtTxt.setAdapter<ArrayAdapter<String>>(cityAdapter)
        val jobType = requireActivity().resources.getStringArray(R.array.jobType)
        val jobTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, jobType)
        binding.jobTypeEdtTxt.setAdapter<ArrayAdapter<String>>(jobTypeAdapter)
    }


    private fun getSelectedRadioBtn(): String =
            when (binding.genderRadioGroup.getCheckedRadioButtonId()) {
                R.id.maleRadioBtn ->
                    "Male"
                R.id.femaleRadioBtn ->
                    "Female"
                else ->
                    "Other"
            }


    private fun showToastError(message: String) {
        Toasty.error(requireContext(), message, Toast.LENGTH_SHORT).show()

    }

    private fun setOnClickListeners() {
        binding.addPhotoBtn.setOnClickListener { choosePhoto() }
        binding.imageView.setOnClickListener { choosePhoto() }
        binding.submitBtn.setOnClickListener {


            if (uri == null) {
                showToastError("Please choose a photo")
                return@setOnClickListener
            }
            val parent = getParentObject()
            viewModel.setEvent(AddParentViewModel.Event.ParentAddSubmitClicked(parent))


        }

        binding.skipBtn.setOnClickListener { findNavController().popBackStack(R.id.addStudentFragment, true) }
        binding.contactEdtTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                binding.apply {
                    if (contactEdtTxt.length() == 10) {
                        ageEdtTxt.requestFocus()
                    }
                    if (contactEdtTxt.length() > 10) {
                        contactEdtTxt.setError("Contact Must have 10 characters")
                    }
                }

            }

            override fun afterTextChanged(s: Editable) {}
        })
    }


    private fun getParentObject(): ParentData {
        val firstName = firstNameEdtTxt.text.toString()
        val lastName = lastNameEdtTxt.text.toString()
        val fullName = "$firstName $lastName"

        val email = emailEdtTxt.text.toString()
        val city = cityEdtTxt.text.toString()
        val contact = contactEdtTxt.text.toString()
        val age = ageEdtTxt.text.toString()
        val jobType = jobTypeEdtTxt.text.toString()
        val jobStatus = jobStatusSpinner.getSelectedItem().toString()
        val gender = getSelectedRadioBtn()!!


        val parent = ParentData()
        parent.firstName = firstName
        parent.lastName = lastName
        parent.fullName = "${parent.firstName} ${parent.lastName}"
        parent.email = email
        parent.city = city
        parent.contact = contact
        parent.age = age
        parent.jobType = jobType
        parent.jobStatus = jobStatus
        parent.gender = gender
        parent.uri = uri

        return parent
    }


    private fun resetEdtTxt() {
        binding.apply {
            firstNameEdtTxt.setText("")
            lastNameEdtTxt.setText("")
            emailEdtTxt.setText("")
            cityEdtTxt.setText("")
            jobTypeEdtTxt.setText("")
            ageEdtTxt.setText("")
            contactEdtTxt.setText("")
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
}
