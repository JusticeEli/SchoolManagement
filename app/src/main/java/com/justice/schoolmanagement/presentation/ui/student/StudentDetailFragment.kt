package com.justice.schoolmanagement.presentation.ui.student

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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
import es.dmoral.toasty.Toasty

class StudentDetailsFragment : Fragment(R.layout.fragment_student_details) {

    private val email: String? = null
    private var studentData: StudentData? = null
    private val studentMarks: StudentMarks? = null
    lateinit var binding: FragmentStudentDetailsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStudentDetailsBinding.bind(view)


        studentData = ApplicationClass.documentSnapshot!!.toObject(StudentData::class.java)
        studentData!!.id = ApplicationClass.documentSnapshot!!.id


        setDefaultValues()
        setOnClickListeners()
        initProgressBar()
    }

    override fun onResume() {
        super.onResume()

        studentData = ApplicationClass.documentSnapshot!!.toObject(StudentData::class.java)
        studentData!!.id = ApplicationClass.documentSnapshot!!.id
        setDefaultValues()

    }


    private fun setOnClickListeners() {
        binding.deleteTxtView.setOnClickListener(View.OnClickListener { deleteStudentDataFromDatabase() })
        binding.editTxtView.setOnClickListener(View.OnClickListener {

            findNavController().navigate(StudentDetailsFragmentDirections.actionStudentDetailsFragmentToEditStudentFragment())
        })
        binding.feesTxtView.setOnClickListener{

            ApplicationClass.studentSnapshot=ApplicationClass.documentSnapshot
            findNavController().navigate(StudentDetailsFragmentDirections.actionStudentDetailsFragmentToFeesFragment(studentData!!.fullName))
        }
    }

    private fun deleteStudentDataFromDatabase() {
        MaterialAlertDialogBuilder(requireContext()).setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", null).setPositiveButton("yes") { dialog, which -> deleteStudentPhoto() }.show()
    }

    private fun deleteStudentPhoto() {
        showProgress(true)
        FirebaseStorage.getInstance().getReferenceFromUrl(studentData!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), "Photo Deleted", Toast.LENGTH_SHORT).show()
                deleteStudentData()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun deleteStudentData() {
        ApplicationClass.documentSnapshot!!.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ApplicationClass.documentSnapshot!!.reference.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ApplicationClass.documentSnapshot = task.result
                        removeStudentMarksFromDatabase()
                        Toasty.success(requireContext(), "Student data Removed", Toast.LENGTH_SHORT).show()
                    } else {
                        val error = task.exception!!.message
                        Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val error = task.exception!!.message
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeStudentMarksFromDatabase() {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_MARKS).document(studentData!!.id).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), "Student Marks removed", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
            findNavController().popBackStack()

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
