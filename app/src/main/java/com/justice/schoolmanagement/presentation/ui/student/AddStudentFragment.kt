package com.justice.schoolmanagement.presentation.ui.student

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentAddStudentBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.presentation.utils.Constants
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import id.zelory.compressor.Compressor
import java.io.File
import java.io.IOException
import java.util.*

class AddStudentFragment : Fragment(R.layout.fragment_add_student) {
    public val COLLECTION_STUDENTS = "students"
    public val COLLECTION_STUDENTS_MARKS = "students_marks"

    private var documentSnapshot: DocumentSnapshot? = null
    private val collectionReferenceMarks = FirebaseFirestore.getInstance().collection(COLLECTION_STUDENTS_MARKS)
    private val collectionReferenceData = FirebaseFirestore.getInstance().collection(COLLECTION_STUDENTS);
    private lateinit var studentData: StudentData
    private lateinit var studentMarks: StudentMarks


    private var uri: Uri? = null
    lateinit var binding: FragmentAddStudentBinding;
    lateinit var progressBar: ProgressBar
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddStudentBinding.bind(view)


        //   initNavigationDrawer();
        setOnClickListeners()
        setValuesForSpinner()
        initProgressBar()
    }

    private fun initProgressBar() {
        progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        binding.relativeLayout.addView(progressBar, params)
        progressBar.isVisible = false
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

    private fun getSelectedRadioBtn(): String? {
        when (binding.genderRadioGroup.getCheckedRadioButtonId()) {
            R.id.maleRadioBtn -> return "Male"
            R.id.femaleRadioBtn -> return "Female"
            R.id.otherRadioBtn -> return "Other"
        }
        return null
    }

    private fun setOnClickListeners() {
        binding.addBtn.setOnClickListener(View.OnClickListener {
            if (uri == null) {
                Toasty.error(requireContext(), "Please choose a photo", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (fieldsAreEmpty()) {
                Toasty.error(requireContext(), "Please Fill All Fields", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            getDataFromEdtTxtAndSaveInDatabase()
        })
        binding.addPhotoBtn.setOnClickListener(View.OnClickListener { choosePhoto() })
        binding.imageView.setOnClickListener(View.OnClickListener { choosePhoto() })
    }

    private fun fieldsAreEmpty(): Boolean {
        binding.apply {
            return if (firstNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || lastNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || emailEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || parentNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || dateOfBirthEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || dateOfArrivalEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || ageEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || cityEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty()) {
                true
            } else false
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

    }

    private fun getDataFromEdtTxtAndSaveInDatabase() {
        // TODO: 13-Apr-20  UN COMMNENT CLASS TEACHER NAME
        binding.apply {
            studentData = StudentData()
            studentData.setFullName(firstNameEdtTxt.getText().toString() + " " + lastNameEdtTxt.getText().toString())
            studentData.setClassGrade(classGradeSpinner.getSelectedItem().toString().toInt())
            studentData.setFirstName(firstNameEdtTxt.getText().toString())
            studentData.setLastName(lastNameEdtTxt.getText().toString())
            studentData.setNationality(nationalitySpinner.getSelectedItem().toString())
            studentData.setReligion(religionSpinner.getSelectedItem().toString())
            studentData.setEmail(emailEdtTxt.getText().toString())
            studentData.setParentName(parentNameEdtTxt.getText().toString())
            studentData.setDateOfBirth(dateOfBirthEdtTxt.getText().toString())
            studentData.setDateOfArrival(dateOfArrivalEdtTxt.getText().toString())
            studentData.setAge(ageEdtTxt.getText().toString())
            studentData.setGender(getSelectedRadioBtn())
            studentData.setClassTeacherName(classTeacherNameSpinner.getSelectedItem().toString())
            studentData.setCity(cityEdtTxt.getText().toString())
            putImageToStorage()
        }

    }

    private fun putDataIntoDatabase() {
        showProgress(true)
        collectionReferenceData.add(studentData!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result!!.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        documentSnapshot = task.result
                        addStudentMarks()
                        resetEdtTxt()
                        Toasty.success(requireContext(), "Student Added ", Toast.LENGTH_SHORT).show()
                        addParent()
                    } else {
                        val error = task.exception!!.message
                        Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private fun showProgress(show: Boolean) {
        progressBar.isVisible=show

    }

    private fun putImageToStorage() {
        val photoName = UUID.randomUUID().toString()
        studentData.setPhotoName(photoName)
        showProgress(true)
        val ref = FirebaseStorage.getInstance().getReference("students_images").child(photoName)
        val uploadTask = ref.putFile(uri!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            showProgress(false)
            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                studentData.setPhoto(downloadUri.toString())
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
        val photoName = UUID.randomUUID().toString()
        studentData.setPhotoName(photoName)
        showProgress(true)
        val thumbnail: Uri
        var compressedImgFile: File? = null
        try {
            compressedImgFile = Compressor(requireActivity()).setCompressFormat(Bitmap.CompressFormat.JPEG).setMaxHeight(10).setMaxWidth(10).setQuality(40).compressToFile(File(uri!!.path))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        thumbnail = Uri.fromFile(compressedImgFile)
        val ref = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_STUDENTS_THUMBNAIL_IMAGES).child(photoName)
        val uploadTask = ref.putFile(thumbnail)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            showProgress(false)
            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                studentData.setThumbnail(downloadUri.toString())
                putDataIntoDatabase()
                Toasty.success(requireContext(), "Photo Uploaded", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }

    private fun addParent() {

        findNavController().navigate(AddStudentFragmentDirections.actionAddStudentFragmentToAddParentFragment(studentData.parentName, studentData.email, true))
    }

    private fun addStudentMarks() {
        studentMarks = StudentMarks()
        studentMarks.setFullName(studentData.getFullName())
        studentMarks.setEmail(studentData.getEmail())
        studentMarks.setClassGrade(studentData.getClassGrade())
        showProgress(true)
        collectionReferenceMarks.document(documentSnapshot!!.id).set(studentMarks).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.info(requireContext(), "Student Marks Added", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }

    private fun setValuesForSpinner() {
        val classGrade = arrayOf("1", "2", "3", "4", "5", "6", "7", "8")
        val arrayAdapter1: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, classGrade)
        binding.classGradeSpinner.setAdapter(arrayAdapter1)
        val cities = arrayOf("Kisumu", "Kitui", "Lamu", "Nairobi", "Machakos", "Marsabit", "Meru", "Migori", "Mombasa", "Nakuru", "Narok", "Trans Nzoia", "Turkana", "Vihiga", "Naivasha", "Eldoret", "Kericho")
        val cityAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, cities)
        binding.cityEdtTxt.setAdapter<ArrayAdapter<String>>(cityAdapter)
        val nationality = arrayOf("Kenyan", "Foreigner")
        val arrayAdapter2: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, nationality)
        binding.nationalitySpinner.setAdapter(arrayAdapter2)
        val religion = arrayOf("Christian", "Muslim")
        val arrayAdapter3: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, religion)
        binding.religionSpinner.setAdapter(arrayAdapter3)
        setValuesForClassTeacherNameSpinner()
    }

    private fun setValuesForClassTeacherNameSpinner() {
        val arrayAdapter4: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), R.layout.spinner_item, ApplicationClass.teacherNames)
        binding.classTeacherNameSpinner.setAdapter(arrayAdapter4)
    }
}

