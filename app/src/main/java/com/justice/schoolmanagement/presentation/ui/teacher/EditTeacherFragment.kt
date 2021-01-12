package com.justice.schoolmanagement.presentation.ui.teacher

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentEditTeacherBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import id.zelory.compressor.Compressor
import java.io.File
import java.io.IOException

class EditTeacherFragment : Fragment(R.layout.fragment_edit_teacher) {

    lateinit var binding: FragmentEditTeacherBinding
    private var uri: Uri? = null
    private var photoChanged = false
    private var photo: String? = null
    private val email: String? = null
    private var teacherData: TeacherData? = null


    val navArgs: EditTeacherFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditTeacherBinding.bind(view)




        teacherData = ApplicationClass.documentSnapshot!!.toObject(TeacherData::class.java)
        teacherData?.setId(ApplicationClass.documentSnapshot!!.id)
        //  initNavigationDrawer();


        //  initNavigationDrawer();
        setDefaultValues()
        setOnClickListeners()
        setUpSubjectsSpinner()
    }

    private fun setUpSubjectsSpinner() {
        val subjects = arrayOf("Math", "Science", "English", "Kiswahili", "sst_cre")
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects)
        binding.subjectSpinner.setAdapter(arrayAdapter)

        binding.contactEdtTxt.setText("07")
    }

    private fun choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(requireActivity())
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

    private fun showProgress(show: Boolean) {
        if (show) {
            Toasty.info(requireContext(), "loading...")
        } else {
            Toasty.info(requireContext(), "finished loading")
        }
    }

    private fun setDefaultValues() {
        binding.apply {
            firstNameEdtTxt.setText(teacherData!!.firstName)
            lastNameEdtTxt.setText(teacherData!!.lastName)
            emailEdtTxt.setText(teacherData!!.email)
            salaryEdtTxt.setText(teacherData!!.salary)
            cityEdtTxt.setText(teacherData!!.city)
            degreeEdtTxt.setText(teacherData!!.degree)
            ageEdtTxt.setText(teacherData!!.age)
            setDefaultValueForGenderRadioBtn()
            setDefaultValueForTypeRadioBtn()
            setDefaultValueForSubjectSpinner()
            contactEdtTxt.setText(teacherData!!.contact)
            uri = Uri.parse(teacherData!!.photo)
            val requestOptions = RequestOptions()
            requestOptions.placeholder(R.mipmap.place_holder)
            Glide.with(requireActivity()).applyDefaultRequestOptions(requestOptions).load(teacherData!!.photo).thumbnail(Glide.with(requireActivity()).load(teacherData!!.thumbnail)).into(imageView)
        }

    }

    private fun setDefaultValueForTypeRadioBtn() {
        when (teacherData!!.type) {
            "teacher" -> binding.teacherRadioBtn.setChecked(true)
            "admin" -> binding.adminRadioBtn.setChecked(true)
        }
    }

    private fun setDefaultValueForGenderRadioBtn() {
        binding.apply {
            when (teacherData!!.gender) {

                "Male" -> maleRadioBtn.setChecked(true)
                "Female" -> femaleRadioBtn.setChecked(true)
                "Other" -> otherRadioBtn.setChecked(true)
                else -> maleRadioBtn.setChecked(true)
            }

        }

    }

    private fun setDefaultValueForSubjectSpinner() {
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
       binding. addPhotoBtn.setOnClickListener(View.OnClickListener { choosePhoto() })
       binding. imageView.setOnClickListener(View.OnClickListener { choosePhoto() })
       binding. submitBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (uri == null) {
                    Toast.makeText(requireContext(), "Please choose a photo", Toast.LENGTH_SHORT).show()
                    return
                }
                if (fieldsAreEmpty()) {
                    Toast.makeText(requireContext(), "Please fill All fields", Toast.LENGTH_SHORT).show()
                    return
                }
                if (!contactEdtTxtFormatIsCorrect()) {
                    return
                }
                dataFromEdtTxtAndAddToDataBase
            }

            // Continue with the task to get the download URL
            private val dataFromEdtTxtAndAddToDataBase: Unit
                private get() {

                    teacherData!!.fullName = binding.firstNameEdtTxt.getText().toString() + " " + binding.lastNameEdtTxt.getText().toString()
                    teacherData!!.firstName = binding.firstNameEdtTxt.getText().toString()
                    teacherData!!.lastName =binding. lastNameEdtTxt.getText().toString()
                    teacherData!!.email =binding. emailEdtTxt.getText().toString()
                    teacherData!!.salary =binding. salaryEdtTxt.getText().toString()
                    teacherData!!.city =binding. cityEdtTxt.getText().toString()
                    teacherData!!.degree =binding. degreeEdtTxt.getText().toString()
                    teacherData!!.age = binding.ageEdtTxt.getText().toString()
                    teacherData!!.gender = getSelectedGenderRadioBtn()
                    teacherData!!.type = getSelectedTypeRadioBtn()
                    teacherData!!.subject = binding.subjectSpinner.getSelectedItem().toString()
                    teacherData!!.contact = binding.contactEdtTxt.getText().toString()
                    if (photoChanged) {
                        teacherData!!.photo = photo
                    }
                    if (photoChanged) {
                        showProgress(true)
                        val ref = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_TEACHERS_IMAGES).child(teacherData!!.id + ".jpg")
                        val uploadTask = ref.putFile(uri!!)
                        uploadTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                throw task.exception!!
                            }
                            // Continue with the task to get the download URL
                            ref.downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUri = task.result
                                photo = downloadUri.toString()
                                teacherData!!.photo = photo
                                uploadThumbnail()
                                Toast.makeText(requireContext(), "Photo Uploaded", Toast.LENGTH_SHORT).show()
                            } else {
                                val error = task.exception!!.message
                                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                            showProgress(false)
                        }
                    } else {
                        putDataInDatabase()
                    }
                }
        })
    }

    private fun uploadThumbnail() {
        var thumbnail: Uri? = null
        var compressedImgFile: File? = null
        try {
            compressedImgFile = Compressor(requireContext()).setCompressFormat(Bitmap.CompressFormat.JPEG).setMaxHeight(10).setMaxWidth(10).setQuality(40).compressToFile(File(uri!!.path))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        thumbnail = Uri.fromFile(compressedImgFile)
        showProgress(true)
        val ref1 = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_TEACHERS_THUMBNAIL_IMAGES).child(teacherData!!.id)
        val uploadTask1 = ref1.putFile(thumbnail)
        uploadTask1.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            ref1.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                teacherData!!.thumbnail = downloadUri.toString()
                putDataInDatabase()
                Toast.makeText(requireContext(), "Thumbnail Uploaded", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }


    private fun putDataInDatabase() {
        showProgress(true)
        ApplicationClass.documentSnapshot!!.reference.set(teacherData!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Teacher Data updated successfully", Toast.LENGTH_SHORT).show()
                ApplicationClass.documentSnapshot!!.reference.get().addOnSuccessListener { documentSnapshot ->
                    ApplicationClass.documentSnapshot = documentSnapshot
                   findNavController().popBackStack()
                }
            } else {
                val error = task.exception!!.message
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }

    private fun getSelectedTypeRadioBtn(): String? {
        when (binding.typeRadioGroup.getCheckedRadioButtonId()) {
            R.id.teacherRadioBtn -> return "teacher"
            R.id.adminRadioBtn -> return "admin"
        }
        return null
    }

    private fun getSelectedGenderRadioBtn(): String? {
        when (binding.genderRadioGroup.getCheckedRadioButtonId()) {
            R.id.maleRadioBtn -> return "Male"
            R.id.femaleRadioBtn -> return "Female"
            R.id.otherRadioBtn -> return "Other"
        }
        return null
    }

    private fun fieldsAreEmpty(): Boolean {
        binding.apply {
            return if (uri == null || firstNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || lastNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || contactEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || emailEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || salaryEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || cityEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || degreeEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || ageEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty()) {
                true
            } else false
        }

    }

}