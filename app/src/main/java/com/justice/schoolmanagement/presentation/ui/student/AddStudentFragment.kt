package com.justice.schoolmanagement.presentation.ui.student

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentAddStudentBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import java.util.*

@AndroidEntryPoint
class AddStudentFragment : Fragment(R.layout.fragment_add_student) {


    private val TAG = "AddStudentFragment"

    private var uri: Uri? = null
    lateinit var binding: FragmentAddStudentBinding;
    lateinit var progressBar: ProgressBar
    private val viewModel: AddStudentViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddStudentBinding.bind(view)
        initProgressBar()
        setOnClickListeners()
        setValuesForSpinner()
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.addStudentStatus.collect {
                Log.d(TAG, "subScribeToObservers: addStudentStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        resetEdtTxt()
                        addParent(it.data!!)
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        Log.d(TAG, "subScribeToObservers: Error:${it.exception?.message}")

                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.loadTeachersNames.collect {
                Log.d(TAG, "subScribeToObservers: loadTeachersNames:${it.status.name}")

                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        viewModel.setEvent(Event.LoadTeachersNames(it.data!!.documents))
                    }
                    Resource.Status.EMPTY -> {
                        //this error shld never occur
                        showProgress(false)
                        showToastInfo("No Teachers Registered")
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToastInfo("Error: ${it.exception?.message}")
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.addStudentEvents.collect {
                when (it) {
                    is Event.SubmitFilteredTeachers -> {
                        setValuesForClassTeacherNameSpinner(it.teachersNames)

                    }
                }
            }
        }
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
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
            }
        }
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(binding.imageView)
    }

    private fun getSelectedRadioBtn(): String {
        when (binding.genderRadioGroup.getCheckedRadioButtonId()) {
            R.id.maleRadioBtn -> return "Male"
            R.id.femaleRadioBtn -> return "Female"
            else -> return "Other"
        }

    }

    private fun setOnClickListeners() {
        binding.addPhotoBtn.setOnClickListener { choosePhoto() }
        binding.imageView.setOnClickListener { choosePhoto() }

        binding.submitBtn.setOnClickListener {
            if (uri == null) {
                Toasty.error(requireContext(), "Please choose a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val student = getStudentObject()
            viewModel.setEvent(Event.StudentAddSubmitClicked(student))

        }
    }

    private fun getStudentObject(): StudentData {
        binding.apply {
            val firstName = firstNameEdtTxt.text.toString()
            val lastName = lastNameEdtTxt.text.toString()
            val fullName = "${firstName} ${lastName}"
            val classGrade = (classGradeSpinner.getSelectedItem().toString())
            val nationality = nationalitySpinner.getSelectedItem().toString()
            val religion = religionSpinner.getSelectedItem().toString()
            val email = emailEdtTxt.text.toString()
            val parentName = parentNameEdtTxt.text.toString()
            val dateOfBirth = dateOfBirthEdtTxt.text.toString()
            val dateOfArrival = dateOfArrivalEdtTxt.text.toString()
            val age = ageEdtTxt.text.toString()
            val gender = getSelectedRadioBtn()
            val classTeacherName = classTeacherNameSpinner.text.toString()
            val city = cityEdtTxt.text.toString()

            val student = StudentData()
            student.firstName = firstName
            student.lastName = lastName
            student.fullName = fullName
            student.classGrade = classGrade
            student.nationality = nationality
            student.religion = religion
            student.email = email
            student.parentName = parentName
            student.dateOfBirth = dateOfBirth
            student.dateOfArrival = dateOfArrival
            student.age = age
            student.gender = gender!!
            student.classTeacherName = classTeacherName
            student.city = city

            student.uri = uri
            return student
        }

    }


    private fun resetEdtTxt() {
        binding.apply {

            firstNameEdtTxt.setText("")
            lastNameEdtTxt.setText("")
            emailEdtTxt.setText("")
            parentNameEdtTxt.setText("")
            dateOfBirthEdtTxt.setText("")
            dateOfArrivalEdtTxt.setText("")
            ageEdtTxt.setText("")
            cityEdtTxt.setText("")
        }
        showProgress(false)
    }


    private fun addParent(student: StudentData) {
        findNavController().navigate(AddStudentFragmentDirections.actionAddStudentFragmentToAddParentFragment(student))
    }


    private fun setValuesForSpinner() {
        val classGrade = requireActivity().resources.getStringArray(R.array.classGrade)
        val arrayAdapter1: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, classGrade)
        binding.classGradeSpinner.setAdapter(arrayAdapter1)
        val cities = requireActivity().resources.getStringArray(R.array.cities)
        val cityAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
        binding.cityEdtTxt.setAdapter<ArrayAdapter<String>>(cityAdapter)
        val nationality = requireActivity().resources.getStringArray(R.array.nationality)
        val arrayAdapter2: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, nationality)
        binding.nationalitySpinner.setAdapter(arrayAdapter2)
        val religion = requireActivity().resources.getStringArray(R.array.religion)
        val arrayAdapter3: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, religion)
        binding.religionSpinner.setAdapter(arrayAdapter3)


    }

    private fun setValuesForClassTeacherNameSpinner(teachersNameList: List<String>) {
        val arrayAdapter4: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, teachersNameList)
        binding.classTeacherNameSpinner.setAdapter(arrayAdapter4)
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
        data class StudentAddSubmitClicked(val student: StudentData) : Event()
        data class LoadTeachersNames(val teachersDocumentSnapshot: List<DocumentSnapshot>) : Event()
        data class SubmitFilteredTeachers(val teachersNames: List<String>) : Event()
    }
}

