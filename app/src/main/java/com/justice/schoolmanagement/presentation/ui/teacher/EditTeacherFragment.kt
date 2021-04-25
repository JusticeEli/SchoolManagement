package com.justice.schoolmanagement.presentation.ui.teacher

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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.edward.nyansapo.wrappers.Resource
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentEditTeacherBinding
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditTeacherFragment : Fragment(R.layout.fragment_edit_teacher) {
    companion object {
        private const val TAG = "EditTeacherFragment"
    }

    lateinit var binding: FragmentEditTeacherBinding
    private var uri: Uri? = null
    private var photoChanged = false

    lateinit var progressBar: ProgressBar
    val navArgs: EditTeacherFragmentArgs by navArgs()
    private val viewModel: EditTeacherViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditTeacherBinding.bind(view)
        initProgressBar()
        setUpSubjectsSpinner()
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {

            launch {
                viewModel.getTeacherToEdit
                        .collect {
                            Log.d(TAG, "subScribeToObservers: status:${it.status.name}")
                            when (it.status) {
                                Resource.Status.LOADING -> {
                                    showProgress(true)
                                }
                                Resource.Status.SUCCESS -> {
                                    Log.d(TAG, "subScribeToObservers: success loading data")
                                    showProgress(false)
                                    viewModel.setCurrentSnapshot(it.data!!)
                                    val teacherData = it.data.toObject(TeacherData::class.java)!!
                                    setDefaultValues(teacherData)
                                    setOnClickListeners()
                                }
                                Resource.Status.ERROR -> {
                                    showProgress(false)
                                    showToastInfo("Error: ${it.exception?.message}")

                                }
                                Resource.Status.EMPTY -> {
                                    showProgress(false)
                                    Log.d(TAG, "subScribeToObservers: document does not exit")
                                }
                            }

                        }
            }

            launch {
                viewModel.editTeacherStatus.collect {
                    Log.d(TAG, "subsribeToObservers: editTeacherStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                            Log.d(TAG, "subsribeToObservers: LOADING:${it.message}")
                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            // causes an error
                            findNavController().popBackStack()
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            Log.d(TAG, "subsribeToObservers: Error:${it.exception?.message}")
                        }
                        Resource.Status.EMPTY -> {
                            showToastInfo("Please Fill All Fields")
                        }

                    }
                }
            }
        }


    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setUpSubjectsSpinner() {
        val subjects = requireActivity().resources.getStringArray(R.array.subjects)
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects)
        binding.subjectSpinner.setAdapter(arrayAdapter)

    }

    private fun choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(requireContext(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                photoChanged = true
                uri = result.uri
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toasty.error(requireContext(), "Error: " + error.message, Toast.LENGTH_SHORT).show()
            }
        }
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(binding.imageView)
    }

    private fun contactEdtTxtFormatIsCorrect(): Boolean {
        val contact: String = binding.contactEdtTxt.getText().toString().trim { it <= ' ' }
        if (!contact.startsWith("07")) {
            Toasty.error(requireContext(), "Contact Must start with 07 !!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (contact.length != 10) {
            Toasty.error(requireContext(), "Contact Must have 10 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }


    private fun setDefaultValues(teacherData: TeacherData) {
        binding.apply {
            firstNameEdtTxt.setText(teacherData!!.firstName)
            lastNameEdtTxt.setText(teacherData!!.lastName)
            emailEdtTxt.setText(teacherData!!.email)
            salaryEdtTxt.setText(teacherData!!.salary)
            cityEdtTxt.setText(teacherData!!.city)
            degreeEdtTxt.setText(teacherData!!.degree)
            ageEdtTxt.setText(teacherData!!.age)
            setDefaultValueForGenderRadioBtn(teacherData)
            setDefaultValueForTypeRadioBtn(teacherData)
            setDefaultValueForSubjectSpinner(teacherData)
            Log.d(TAG, "setDefaultValues: ${teacherData!!.contact}")
            contactEdtTxt.setText(teacherData!!.contact)
            uri = Uri.parse(teacherData!!.photo)
            val requestOptions = RequestOptions()
            requestOptions.placeholder(R.mipmap.place_holder)
            Glide.with(requireActivity()).applyDefaultRequestOptions(requestOptions).load(teacherData!!.photo).thumbnail(Glide.with(requireActivity()).load(teacherData!!.thumbnail)).into(imageView)
        }

    }

    private fun setDefaultValueForTypeRadioBtn(teacherData: TeacherData) {
        when (teacherData!!.type) {
            "teacher" -> binding.teacherRadioBtn.setChecked(true)
            "admin" -> binding.adminRadioBtn.setChecked(true)
        }
    }

    private fun setDefaultValueForGenderRadioBtn(teacherData: TeacherData) {
        binding.apply {
            when (teacherData!!.gender) {

                "Male" -> maleRadioBtn.setChecked(true)
                "Female" -> femaleRadioBtn.setChecked(true)
                "Other" -> otherRadioBtn.setChecked(true)
                else -> maleRadioBtn.setChecked(true)
            }

        }

    }

    private fun setDefaultValueForSubjectSpinner(teacherData: TeacherData) {
        binding.apply {
            when (teacherData!!.subject) {
                "Math" -> subjectSpinner.setSelection(0)
                "Science" -> subjectSpinner.setSelection(1)
                "English" -> subjectSpinner.setSelection(2)
                "Kiswahili" -> subjectSpinner.setSelection(3)
                "sst_cre" -> subjectSpinner.setSelection(4)
            }
        }

    }

    private fun setOnClickListeners() {
        binding.addPhotoBtn.setOnClickListener { choosePhoto() }
        binding.imageView.setOnClickListener { choosePhoto() }
        binding.submitBtn.setOnClickListener {

            Log.d(TAG, "setOnClickListeners: submitBtn Clicked")
            if (uri == null) {
                Toasty.error(requireContext(), "Please choose a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val teacherData = viewModel.currentSnapshot.value!!.toObject(TeacherData::class.java)!!
            val teacher = getTeacherObject()
            Log.d(TAG, "setOnClickListeners: teacher:$teacher")
            teacher.photo = teacherData.photo
            teacher.thumbnail = teacherData.thumbnail
            viewModel.setEvent(EditTeacherFragment.Event.TeacherEditSubmitClicked(teacher, photoChanged))
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
            teacherData.type = getSelectedTypeRadioBtn()
            teacherData.subject = subjectSpinner.selectedItem.toString()

            teacherData.uri = uri
        }
        return teacherData

    }

    private fun getSelectedTypeRadioBtn(): String {
        when (binding.typeRadioGroup.getCheckedRadioButtonId()) {
            R.id.teacherRadioBtn -> return "teacher"
            else -> return "admin"
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
        data class TeacherEditSubmitClicked(val teacher: TeacherData, val photoChanged: Boolean) : Event()
    }

}