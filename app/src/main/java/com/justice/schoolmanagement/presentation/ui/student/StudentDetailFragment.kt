package com.justice.schoolmanagement.presentation.ui.student
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentStudentDetailsBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.presentation.utils.Constants
import com.justice.schoolmanagement.student.EditStudentActivity
import es.dmoral.toasty.Toasty

class StudentDetailsFragment:Fragment(R.layout.fragment_student_details) {

    private val email: String? = null
    private var studentData: StudentData? = null
    private val studentMarks: StudentMarks? = null
    lateinit var binding:FragmentStudentDetailsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentStudentDetailsBinding.bind(view)


        studentData = ApplicationClass.documentSnapshot!!.toObject(StudentData::class.java)
        studentData!!.id = ApplicationClass.documentSnapshot!!.id


        setDefaultValues()
        setOnClickListeners()

    }

    override fun onResume() {
        super.onResume()

        studentData = ApplicationClass.documentSnapshot!!.toObject(StudentData::class.java)
        studentData!!.id = ApplicationClass.documentSnapshot!!.id
        setDefaultValues()

    }


    private fun setOnClickListeners() {
       binding. deleteTxtView.setOnClickListener(View.OnClickListener { deleteStudentDataFromDatabase() })
       binding. editTxtView.setOnClickListener(View.OnClickListener {
            val intent = Intent(requireContext(), EditStudentActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        })
    }

    private fun deleteStudentDataFromDatabase() {
        MaterialAlertDialogBuilder(requireContext()).setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", null).setPositiveButton("yes") { dialog, which -> deleteStudent() }.show()
    }

    private fun deleteStudent() {
        showProgress(true)
        FirebaseStorage.getInstance().getReferenceFromUrl(studentData!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), "Photo Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
        ApplicationClass.documentSnapshot!!.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showProgress(true)
                ApplicationClass.documentSnapshot!!.reference.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ApplicationClass.documentSnapshot = task.result
                        removeStudentMarksFromDatabase()
                        Toasty.success(requireContext(), "Student data Removed", Toast.LENGTH_SHORT).show()
                     findNavController().popBackStack()
                    } else {
                        val error = task.exception!!.message
                        Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                    showProgress(false)
                }
            } else {
                val error = task.exception!!.message
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }

    private fun removeStudentMarksFromDatabase() {
        showProgress(true)
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_STUDENTS_MARKS).document(studentData!!.id).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), "Student Marks removed", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }


    private fun setDefaultValues() {
        binding.apply {
            studentNameTxtView.setText(studentData!!.fullName)
            studentClassTxtView.setText("" + studentData!!.classGrade)
            firstNameTxtView.setText(studentData!!.firstName)
            lastNameTxtView.setText(studentData!!.lastName)
            classGradeTxtView.setText("" + studentData!!.classGrade)
            nationalityTxtView.setText(studentData!!.nationality)
            religionTxtView.setText(studentData!!.religion)
            emailTxtView.setText(studentData!!.email)
            parentNameTxtView.setText(studentData!!.parentName)
            dateOfBirthTxtView.setText(studentData!!.dateOfBirth)
            dateOfArrivalTxtView.setText(studentData!!.dateOfArrival)
            ageTxtView.setText("" + studentData!!.age)
            genderTxtView.setText(studentData!!.gender)
            classTeacherNameTxtView.setText(studentData!!.classTeacherName)
            cityTxtView.setText(studentData!!.city)
        }

        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(studentData!!.photo).thumbnail(Glide.with(this).load(studentData!!.thumbnail)).into(binding.imageView)
    }

    /////////////////////PROGRESS_BAR////////////////////////////
        private fun showProgress(show: Boolean) {
            if (show) {
             Toasty.info(requireContext(),"loading...")
            } else {
                Toasty.info(requireContext(),"finished loading")
            }
        }
    }
