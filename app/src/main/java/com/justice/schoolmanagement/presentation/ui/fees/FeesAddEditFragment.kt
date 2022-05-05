package com.justice.schoolmanagement.presentation.ui.fees

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentFeesAddEditBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class FeesAddEditFragment : Fragment(R.layout.fragment_fees_add_edit) {

    private val TAG = "FeesAddEditFragment"

    lateinit var binding: FragmentFeesAddEditBinding
    private val viewModel: FeesAddEditViewModel by viewModels()
    private val navArgs: FeesAddEditFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeesAddEditBinding.bind(view)
        initProgressBar()
        Log.d(TAG, "onViewCreated: fees:${navArgs.studentFees}")
        Log.d(TAG, "onViewCreated: student:${navArgs.studentData}")

        setOnClickListeners()
        subScribeToObservers()
        viewModel.setEvent(Event.GetStudent(navArgs.studentData))


    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.addEditEvents.collect {
                when (it) {
                    is Event.CheckUpdating -> {
                        startUpdate(it.updating)
                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getStudent.collect {
                Log.d(TAG, "subScribeToObservers: getStudent:${it.status.name}")
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        viewModel.setCurrentStudent(it.data!!)
                        viewModel.setEvent(Event.CheckUpdating(studentFees = navArgs.studentFees))

                    }
                    Resource.Status.ERROR -> {

                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.studentFeesStatus.collect {
                Log.d(TAG, "subScribeToObservers: studentFeesStatus :${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        viewModel.setCurrentFees(it.data!!)
                        setInitialValues(it.data!!)

                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToastInfo("Error: ${it.exception?.message}")

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.addEditFeesStatus.collect {
                Log.d(TAG, "subScribeToObservers: addEditFeesStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)

                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToastInfo("Error: ${it.exception?.message}")

                    }
                }
            }
        }
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun startUpdate(updating: Boolean) {
        viewModel.setIsUpdating(updating)

        if (updating) {
            viewModel.setEvent(Event.FetchFees(navArgs.studentFees!!))
        }
    }

    private fun setOnClickListeners() {
        binding.saveBtn.setOnClickListener {
            val feesObject = getFeesObject()
            viewModel.setEvent(Event.AddEditFees(feesObject))
        }
    }

    private fun getFeesObject(): StudentFees {
        val studentFees = StudentFees()
        studentFees.payedAmount = binding.payedEdtTxt.text.toString()
        return studentFees
    }


    private fun setInitialValues(snapshot: DocumentSnapshot) {
        val studentFees = snapshot.toObject(StudentFees::class.java)!!
        binding.payedEdtTxt.setText(studentFees.payedAmount)
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
        data class CheckUpdating(val updating: Boolean = false,val studentFees: StudentFees?=null) : Event()
        data class FetchFees (val studentFees: StudentFees): Event()
        data class GetStudent (val studentData: StudentData): Event()
        data class AddEditFees(val studentFees: StudentFees) : Event()

    }
}