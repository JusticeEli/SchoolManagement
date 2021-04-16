package com.justice.schoolmanagement.presentation.ui.student

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.*
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.alldata.AllData
import com.justice.schoolmanagement.databinding.FragmentEditStudentBinding
import com.justice.schoolmanagement.presentation.SchoolApplication
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import id.zelory.compressor.Compressor
import java.io.File
import java.io.IOException
import java.util.*

class EditStudentFragment : Fragment(R.layout.fragment_edit_student) {
    private var email: String? = null
    private lateinit var studentData: StudentData
    private lateinit var studentMarks: StudentMarks

    private var uri: Uri? = null
    private var photoChanged = false
    lateinit var binding: FragmentEditStudentBinding
    lateinit var progressBar: ProgressBar
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditStudentBinding.bind(view)

        studentData = SchoolApplication.documentSnapshot!!.toObject(StudentData::class.java)!!
        studentData!!.id = SchoolApplication.documentSnapshot!!.id

        initProgressBar()

        //   initNavigationDrawer();
        setDefaultValuesToEdtTxt()
        setOnClickListeners()
        setValuesForSpinner()
     }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                uri = result.uri
                photoChanged = true
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(binding.imageView)
    }

    private fun setDefaultValueForRadioBtn() {
        binding.apply {
            when (studentData!!.gender) {
                "Male" -> maleRadioBtn.setChecked(true)
                "Female" -> femaleRadioBtn.setChecked(true)
                "Other" -> otherRadioBtn.setChecked(true)
            }
        }

    }

    private fun setDefaultValuesToEdtTxt() {
        binding.apply {
            firstNameEdtTxt.setText(studentData!!.firstName)
            lastNameEdtTxt.setText(studentData!!.lastName)
            setDefaultValueClassGradeSpinner()
            setDefaultValueForNationalitySpinner()
            setDefaultValueForReligionSpinner()
            setDefaultValueForClassTeacherNameSpinner()
            setDefaultValueForRadioBtn()
            emailEdtTxt.setText(studentData!!.email)
            parentNameEdtTxt.setText(studentData!!.parentName)
            dateOfBirthEdtTxt.setText(studentData!!.dateOfBirth)
            dateOfArrivalEdtTxt.setText(studentData!!.dateOfArrival)
            ageEdtTxt.setText(studentData!!.age)
            cityEdtTxt.setText(studentData!!.city)
            uri = Uri.parse(studentData!!.photo)
        }

        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(studentData!!.photo).thumbnail(Glide.with(requireActivity()).load(studentData!!.thumbnail)).into(binding.imageView)
    }

    private fun setDefaultValueForClassTeacherNameSpinner() {
        binding.classTeacherNameSpinner.setSelection(SchoolApplication.teacherNames.indexOf(studentData!!.classTeacherName))
    }

    private fun setDefaultValueForReligionSpinner() {
        when (studentData!!.religion) {
            "Christian" -> binding.religionSpinner.setSelection(0)
            "Muslim" -> binding.religionSpinner.setSelection(1)
        }
    }

    private fun setDefaultValueForNationalitySpinner() {
        when (studentData!!.nationality) {
            "Kenyan" -> binding.nationalitySpinner.setSelection(0)
            "Foreigner" -> binding.nationalitySpinner.setSelection(1)
        }
    }

    private fun setDefaultValueClassGradeSpinner() {
        binding.apply {
            when (studentData!!.classGrade) {
                1 -> classGradeSpinner.setSelection(0)
                2 -> classGradeSpinner.setSelection(1)
                3 -> classGradeSpinner.setSelection(2)
                4 -> classGradeSpinner.setSelection(3)
                5 -> classGradeSpinner.setSelection(4)
                6 -> classGradeSpinner.setSelection(5)
                7 -> classGradeSpinner.setSelection(6)
                8 -> classGradeSpinner.setSelection(7)
            }
        }

    }

    private fun setOnClickListeners() {
        binding.submitBtn.setOnClickListener(View.OnClickListener {
            if (uri == null) {
                Toasty.error(requireContext(), "Please choose a photo", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (fieldsAreEmpty()) {
                Toasty.error(requireContext(), "Please Fill All Fields", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            getDataFromEdtTxtAndUpdateInDatabase()
            Toasty.success(requireContext(), studentData!!.firstName + " Edited Successfully ", Toast.LENGTH_SHORT).show()
        })
        binding.addPhotoBtn.setOnClickListener(View.OnClickListener { choosePhoto() })
        binding.imageView.setOnClickListener(View.OnClickListener { choosePhoto() })
    }

    private fun fieldsAreEmpty(): Boolean {
        binding.apply {
            return if (uri == null || firstNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || lastNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || emailEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || parentNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || dateOfBirthEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || dateOfArrivalEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || ageEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || cityEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty()) {
                true
            } else false
        }

    }

    private fun choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(requireContext(), this);
    }

    private fun getSelectedRadioBtn(): String? {
        when (binding.genderRadioGroup.getCheckedRadioButtonId()) {
            R.id.maleRadioBtn -> return "Male"
            R.id.femaleRadioBtn -> return "Female"
            R.id.otherRadioBtn -> return "Other"
        }
        return null
    }

    private fun getDataFromEdtTxtAndUpdateInDatabase() {
// TODO: 13-Apr-20 CHOOSE TEACHER EDIT TEXT HAS A PROBLEM
        binding.apply {
            studentData!!.fullName = firstNameEdtTxt.getText().toString().trim { it <= ' ' } + " " + lastNameEdtTxt.getText().toString().trim { it <= ' ' }
            studentData!!.firstName = firstNameEdtTxt.getText().toString().trim { it <= ' ' }
            studentData!!.lastName = lastNameEdtTxt.getText().toString().trim { it <= ' ' }
            studentData!!.classGrade = classGradeSpinner.getSelectedItem().toString().trim { it <= ' ' }.toInt()
            studentData!!.nationality = nationalitySpinner.getSelectedItem().toString().trim { it <= ' ' }
            studentData!!.religion = religionSpinner.getSelectedItem().toString().trim { it <= ' ' }
            studentData!!.classTeacherName = classTeacherNameSpinner.getSelectedItem().toString().trim { it <= ' ' }
            studentData!!.gender = getSelectedRadioBtn()
            studentData!!.email = emailEdtTxt.getText().toString().trim { it <= ' ' }
            studentData!!.parentName = parentNameEdtTxt.getText().toString().trim { it <= ' ' }
            studentData!!.dateOfBirth = dateOfBirthEdtTxt.getText().toString().trim { it <= ' ' }
            studentData!!.dateOfArrival = dateOfArrivalEdtTxt.getText().toString().trim { it <= ' ' }
            studentData!!.age = ageEdtTxt.getText().toString().trim { it <= ' ' }
            studentData!!.city = cityEdtTxt.getText().toString().trim { it <= ' ' }

        }

        if (photoChanged) {
            showProgress(true)
            val ref = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_IMAGES).child(studentData!!.photoName)
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
                    studentData!!.photo = downloadUri.toString()
                    uploadThumbnail()
                    Toast.makeText(requireContext(), "Photo Uploaded", Toast.LENGTH_SHORT).show()
                } else {
                    val error = task.exception!!.message
                    Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }

            /////////////////////////////////////////////
        } else {
            updateInDatabase()
        }
    }

    private fun uploadThumbnail() {
        val photoName = UUID.randomUUID().toString()
        studentData!!.photoName = photoName
        showProgress(true)
        val thumbnail: Uri
        var compressedImgFile: File? = null
        try {
            compressedImgFile = Compressor(requireActivity()).setCompressFormat(Bitmap.CompressFormat.JPEG).setMaxHeight(10).setMaxWidth(10).setQuality(40).compressToFile(File(uri!!.path))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        thumbnail = Uri.fromFile(compressedImgFile)
        val ref = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_THUMBNAIL_IMAGES).child(photoName)
        val uploadTask = ref.putFile(thumbnail)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                studentData!!.thumbnail = downloadUri.toString()
                updateInDatabase()
                Toasty.success(requireContext(), "Thumbnail Uploaded", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateInDatabase() {
        showProgress(true)
        SchoolApplication.documentSnapshot!!.reference.set(studentData!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showProgress(true)
                SchoolApplication.documentSnapshot!!.reference.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        SchoolApplication.documentSnapshot = task.result
                        updateStudentMarks()
                        Toasty.success(requireContext(), "Student Data Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        val error = task.exception!!.message
                        Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStudentMarks() {
        studentMarks = StudentMarks()
        studentMarks.setFullName(studentData!!.fullName)
        studentMarks.setEmail(studentData!!.email)
        studentMarks.setClassGrade(studentData!!.classGrade)
        val map: MutableMap<String, Any> = HashMap()
        map["fullName"] = studentData!!.fullName
        map["classGrade"] = studentData!!.classGrade
        map["email"] = studentData!!.email
        showProgress(true)
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_MARKS).document(studentData!!.id).get().addOnSuccessListener { documentSnapshot ->
            studentMarks = documentSnapshot.toObject(StudentMarks::class.java)!!
            studentMarks!!.fullName = studentData!!.fullName
            studentMarks!!.classGrade = studentData!!.classGrade
            studentMarks!!.email = studentData!!.email

            ///////////////////////////
            FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_MARKS).document(studentData!!.id).set(studentMarks!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toasty.success(requireContext(), "Student Marks updated", Toast.LENGTH_SHORT).show()
                } else {
                    val error = task.exception!!.message
                    Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
                }
                showProgress(false)
                findNavController().popBackStack(R.id.studentsFragment, false)

            }
        }
    }


    fun setValuesForSpinner() {
        val classGrade = arrayOf("1", "2", "3", "4", "5", "6", "7", "8")
        val arrayAdapter1: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, classGrade)
        binding.classGradeSpinner.setAdapter(arrayAdapter1)
        val cities = arrayOf("Kisumu", "Kitui", "Lamu", "Machakos", "Marsabit", "Meru", "Migori", "Mombasa", "Nakuru", "Narok", "Trans Nzoia", "Turkana", "Vihiga", "Naivasha", "Eldoret", "Kericho")
        val cityAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, cities)
        binding.cityEdtTxt.setAdapter<ArrayAdapter<String>>(cityAdapter)
        val nationality = arrayOf("Kenyan", "Foreigner")
        val arrayAdapter2: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, nationality)
        binding.nationalitySpinner.setAdapter(arrayAdapter2)
        val religion = arrayOf("Christian", "Muslim")
        val arrayAdapter3: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, religion)
        binding.religionSpinner.setAdapter(arrayAdapter3)
        loadTeacherNames()
    }

    private fun setValuesForClassTeacherNameSpinner() {


        val arrayAdapter4: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, SchoolApplication.teacherNames)
        binding.classTeacherNameSpinner.setAdapter(arrayAdapter4)
    }

    fun loadTeacherNames() {
        showProgress(true)
        SchoolApplication.teacherNames.clear()
        AllData.teacherDataList.clear()
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS).get().addOnCompleteListener { task ->
            //   Toast.makeText(this@ApplicationClass, "Loading Teachers name: ", Toast.LENGTH_SHORT).show()
            if (task.isSuccessful) {
                SchoolApplication.teacherNames.clear()
                for (documentSnapshot in task.result!!) {
                    SchoolApplication.teacherNames.add(documentSnapshot.toObject(TeacherData::class.java).fullName)
                    AllData.teacherDataList.add(documentSnapshot.toObject(TeacherData::class.java))
                }
                setValuesForClassTeacherNameSpinner()

            } else {
                Toast.makeText(requireContext(), "Error: " + task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
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


