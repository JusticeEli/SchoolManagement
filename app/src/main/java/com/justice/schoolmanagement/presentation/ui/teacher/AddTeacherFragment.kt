package com.justice.schoolmanagement.presentation.ui.teacher

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentAddTeacherBinding
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import id.zelory.compressor.Compressor
import java.io.File
import java.io.IOException

@Suppress("DEPRECATION")
class AddTeacherFragment : Fragment(R.layout.fragment_add_teacher) {

    private var teacherData: TeacherData? = null
    private var teacherId: String? = null


    private val collectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_TEACHERS)
    private var uri: Uri? = null

    lateinit var binding: FragmentAddTeacherBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        binding = FragmentAddTeacherBinding.bind(view)
        setDefaultValues()
        //   initNavigationDrawer();

        //   initNavigationDrawer();
        setOnClickListeners()
        setUpSubjectsSpinner();

    }


    private fun setUpSubjectsSpinner() {
        val subjects = arrayOf("Math", "Science", "English", "Kiswahili", "sst_cre")
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
        if (item.itemId == R.id.logoutMenu) {
            AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener {
                        findNavController().popBackStack()
                    }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setOnClickListeners() {

        binding.apply {

            addPhotoBtn.setOnClickListener(View.OnClickListener { choosePhoto() })
            imageView.setOnClickListener(View.OnClickListener { choosePhoto() })
            addBtn.setOnClickListener(View.OnClickListener { getDataFromEdtTxtAndAddItToDatabase() })
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

    private fun choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(requireActivity())
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
        requestOptions.centerCrop()
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(binding.imageView)
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

    private fun fieldsAreEmpty(): Boolean {
        binding.apply {
            return if (uri == null || firstNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || lastNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || contactEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || salaryEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || cityEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || degreeEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || ageEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty()) {
                true
            } else false

        }

    }

    private fun getDataFromEdtTxtAndAddItToDatabase() {
        if (uri == null) {
            Toasty.info(requireContext(), "Please choose a photo", Toast.LENGTH_SHORT).show()
            return
        }
        if (fieldsAreEmpty()) {
            Toasty.error(requireContext(), "Please Fill All Fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (!contactEdtTxtFormatIsCorrect()) {
            return
        }
        binding.apply {
            teacherData = TeacherData(firstNameEdtTxt.getText().toString().trim { it <= ' ' } + " " + lastNameEdtTxt.getText().toString().trim { it <= ' ' }, firstNameEdtTxt.getText().toString().trim { it <= ' ' }, lastNameEdtTxt.getText().toString().trim { it <= ' ' }, FirebaseAuth.getInstance().currentUser!!.email, salaryEdtTxt.getText().toString().trim { it <= ' ' }, cityEdtTxt.getText().toString().trim { it <= ' ' }, degreeEdtTxt.getText().toString().trim { it <= ' ' }, ageEdtTxt.getText().toString().trim { it <= ' ' }, getSelectedGenderRadioBtn(), "teacher", "photo", subjectSpinner.getSelectedItem().toString(), contactEdtTxt.getText().toString().trim { it <= ' ' })
            registerTeacherAndPutDataInDatabase()
        }

    }

    private fun contactEdtTxtFormatIsCorrect(): Boolean {
        val contact: String = binding.contactEdtTxt.getText().toString().trim { it <= ' ' }
        if (!contact.startsWith("07")) {
            Toasty.error(requireContext(), "Contact Must start with 07 !!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (contact.length != 10) {
            Toasty.info(requireContext(), "Contact Must have 10 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerTeacherAndPutDataInDatabase() {
        teacherId = FirebaseAuth.getInstance().uid
        putTeacherPhotoInDatabase()
    }

    private fun putTeacherPhotoInDatabase() {


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        showProgress(true)
        val ref = FirebaseStorage.getInstance().getReference("teachers_images").child("$teacherId.jpg")
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
                teacherData!!.photo = downloadUri.toString()
                uploadThumbnail()
                Toasty.success(requireContext(), "Photo Uploaded", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }

        /////////////////////////////////////////////
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
        val ref1 = FirebaseStorage.getInstance().getReference("teachers_thumbnail_images").child(teacherId!!)
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
                putTeacherDataInDatabase()
                Toasty.info(requireContext(), "Thumbnail Uploaded", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }


    private fun putTeacherDataInDatabase() {
        showProgress(true)
        collectionReference.document(FirebaseAuth.getInstance().uid!!).set(teacherData!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "putTeacherDataInDatabase: ")
                Toasty.info(requireContext(), "Teacher Data Saved", Toast.LENGTH_SHORT).show()
                resetEdtTxt()
                findNavController().popBackStack()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }


    private fun getSelectedGenderRadioBtn(): String? {
        when (binding.genderRadioGroup.getCheckedRadioButtonId()) {
            R.id.maleRadioBtn -> return "Male"
            R.id.femaleRadioBtn -> return "Female"
            R.id.otherRadioBtn -> return "Other"
        }
        return null
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private fun showProgress(show: Boolean) {
        if (show) {
          Toasty.info(requireContext(),"loading...")
        } else {
            Toasty.info(requireContext(),"loading finished")
        }
    }
  companion object {
          private  const val TAG="AddTeacherFragment"
      }
}