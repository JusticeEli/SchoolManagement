package com.justice.schoolmanagement.presentation.ui.teacher

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
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
import kotlinx.android.synthetic.main.fragment_add_parent.*
import java.io.File
import java.io.IOException


class AddTeacherFragment : Fragment(R.layout.fragment_add_teacher) {

    private var teacherData: TeacherData? = null
    private var teacherId: String? = null


    private val collectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS)
    private var uri: Uri? = null
    lateinit var progressBar: ProgressBar
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
        initProgressBar()
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
        Log.d(TAG, "onOptionsItemSelected: item selected")
        if (item.itemId == R.id.logoutMenu) {
            Log.d(TAG, "onOptionsItemSelected: logout pressed")
            AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnSuccessListener {
                        Log.d(TAG, "onOptionsItemSelected: signed out")
                        findNavController().popBackStack()
                    }

        }
        return true

    }

    private fun setOnClickListeners() {

        binding.apply {

            addPhotoBtn.setOnClickListener(View.OnClickListener { choosePhoto() })
            imageView.setOnClickListener(View.OnClickListener { choosePhoto() })
            binding.submitBtn.setOnClickListener(View.OnClickListener {
                Log.d(TAG, "setOnClickListeners: add btn pressed")
                binding.submitBtn.isVisible = false
                getDataFromEdtTxtAndAddItToDatabase()
            })
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
showProgress(false)
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
            submit_btn.isVisible = true
            return
        }
        if (fieldsAreEmpty()) {
            Toasty.error(requireContext(), "Please Fill All Fields", Toast.LENGTH_SHORT).show()
            submit_btn.isVisible = true
            return
        }
        if (!contactEdtTxtFormatIsCorrect()) {
            submit_btn.isVisible = true
            return
        }
        binding.apply {

            val fullName = firstNameEdtTxt.getText().toString().trim() + " " + lastNameEdtTxt.getText().toString().trim()
            val firstName = firstNameEdtTxt.getText().toString().trim()
            val lastName = lastNameEdtTxt.getText().toString().trim()
            val email = emailEdtTxt.getText().toString().trim()
            val salary = salaryEdtTxt.getText().toString().trim()
            val city = cityEdtTxt.getText().toString().trim()
            val degree = degreeEdtTxt.getText().toString().trim()
            val age = ageEdtTxt.getText().toString().trim()
            val contact = contactEdtTxt.getText().toString().trim()




            teacherData = TeacherData(firstNameEdtTxt.getText().toString().trim() + " " + lastNameEdtTxt.getText().toString().trim(), firstNameEdtTxt.getText().toString().trim(), lastNameEdtTxt.getText().toString().trim(), emailEdtTxt.getText().toString().trim(), salaryEdtTxt.getText().toString().trim(), cityEdtTxt.getText().toString().trim(), degreeEdtTxt.getText().toString().trim(), ageEdtTxt.getText().toString().trim(), getSelectedGenderRadioBtn(), "admin", "photo", subjectSpinner.getSelectedItem().toString(), contactEdtTxt.getText().toString().trim())


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
        val ref = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS_IMAGES).child("$teacherId.jpg")
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
        val ref1 = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS_THUMBNAIL_IMAGES).child(teacherId!!)
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

        }
    }


    private fun putTeacherDataInDatabase() {
        showProgress(true)
        collectionReference.document(FirebaseAuth.getInstance().uid!!).set(teacherData!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "putTeacherDataInDatabase: ")
                Toasty.info(requireContext(), "Teacher Data Saved", Toast.LENGTH_SHORT).show()
                resetEdtTxt()
                Log.d(TAG, "putTeacherDataInDatabase: going to dashboard fragment")
                findNavController().navigate(R.id.action_addTeacherFragment_to_dashboardFragment)
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }

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
    companion object {
        private const val TAG = "AddTeacherFragment"
    }
}