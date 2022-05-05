package com.justice.schoolmanagement.presentation.ui.student

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentStudentDetailsBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class StudentDetailsFragment : Fragment(R.layout.fragment_student_details) {

    private val TAG = "StudentDetailFragment"

    @Inject
    lateinit var requestManager: RequestManager
    lateinit var binding: FragmentStudentDetailsBinding
    private val viewModel: StudentDetailsViewModel by viewModels()
    private val navArgs: StudentDetailsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ${navArgs.studentData}")
        binding = FragmentStudentDetailsBinding.bind(view)
        initProgressBar()
        subScribeToObservers()
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun subScribeToObservers() {

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.currentStudent.collect {
                Log.d(TAG, "subScribeToObservers: currentStudent:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {

                    }
                    Resource.Status.SUCCESS -> {
                        viewModel.setCurrentSnapshot(it.data!!)
                        setDefaultValues(it.data!!.toObject(StudentData::class.java)!!)
                        setOnClickListeners()

                    }
                    Resource.Status.ERROR -> {
                        showToastInfo("Error:${it.exception?.message}")
                    }
                }

            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            Log.d(TAG, "subScribeToObservers: studentDetailEvents")
            viewModel.studentDetailEvents.collect {
                when (it) {
                    is Event.StudentEdit -> {
                        goToStudentEditScreen(it.snapshot)
                    }
                    is Event.StudentDelete -> {
                        deleteStudentDataFromDatabase(it.snapshot)
                    }
                    is Event.FeesClicked -> {
                        goToFeesScreen(it.snapshot)
                    }

                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.deleteStudentStatus.collect {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        showToastInfo("Success deleting student")
                        findNavController().popBackStack()
                    }
                    Resource.Status.ERROR -> {
                        showToastInfo("Error: ${it.exception?.message}")
                    }
                }
            }
        }
    }

    private fun goToFeesScreen(snapshot: DocumentSnapshot) {
        val student = snapshot.toObject(StudentData::class.java)!!
        Log.d(TAG, "goToFeesScreen: student:$student")
        findNavController().navigate(StudentDetailsFragmentDirections.actionStudentDetailsFragmentToFeesFragment(student,student.fullName))

    }

    private fun goToStudentEditScreen(snapshot: DocumentSnapshot) {
        val studentData = snapshot.toObject(StudentData::class.java)!!
        findNavController().navigate(StudentDetailsFragmentDirections.actionStudentDetailsFragmentToEditStudentFragment(studentData))
    }


    private fun setOnClickListeners() {
        binding.deleteTxtView.setOnClickListener {
            viewModel.setEvent(Event.StudentDelete(viewModel.currentSnapshot.value!!))

        }
        binding.editTxtView.setOnClickListener {
            viewModel.setEvent(Event.StudentEdit(viewModel.currentSnapshot.value!!))
        }
        binding.feesTxtView.setOnClickListener {
            viewModel.setEvent(Event.FeesClicked(viewModel.currentSnapshot.value!!))
        }
    }

    private fun deleteStudentDataFromDatabase(snapshot: DocumentSnapshot) {
        MaterialAlertDialogBuilder(requireContext()).setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", null).setPositiveButton("yes") { dialog, which ->
            viewModel.setEvent(Event.StudentDeleteConfirmed(snapshot))

        }.show()
    }


    private fun setDefaultValues(studentData: StudentData) {
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


        requestManager.load(studentData!!.photo).thumbnail(requestManager.load(studentData!!.thumbnail)).into(binding.imageView)
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


    sealed class Event {
        data class StudentDeleteConfirmed(val snapshot: DocumentSnapshot) : Event()
        data class StudentDelete(val snapshot: DocumentSnapshot) : Event()
        data class StudentEdit(val snapshot: DocumentSnapshot) : Event()
        data class FeesClicked(val snapshot: DocumentSnapshot) : Event()

    }
}
