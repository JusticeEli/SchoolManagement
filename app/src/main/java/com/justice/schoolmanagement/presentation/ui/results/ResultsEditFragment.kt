package com.justice.schoolmanagement.presentation.ui.results

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
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentResultsEditBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_fees.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ResultsEditFragment : Fragment(R.layout.fragment_results_edit) {

    private val TAG = "ResultsEditFragment"


    lateinit var binding: FragmentResultsEditBinding

    private val viewModel: ResultsEditViewModel by viewModels()
    private val navArgs: ResultsEditFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResultsEditBinding.bind(view)
        initProgressBar()
        Log.d(TAG, "onViewCreated: studentMarks:${navArgs.studentMarks}")
        setOnClickListeners()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            subscribeToObservers2()

        }


    }

    private  suspend fun subscribeToObservers2() {

        viewModel.getStudentMarks.collect {
            Log.d(TAG, "subscribeToObservers: getStudentMarks:${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {
                    //   showProgress(true)

                }
                Resource.Status.SUCCESS -> {
                    //    showProgress(false)
                    viewModel.setCurrentStudentMarks(it.data!!)
                    setDefaultValues(it.data!!)

                }
                Resource.Status.ERROR -> {
                    //   showProgress(false)

                }
            }
        }


    }

    private suspend fun subscribeToObservers() {

        viewModel.getStudentMarks.collect {
            Log.d(TAG, "subscribeToObservers: getStudentMarks:${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {
                    //   showProgress(true)

                }
                Resource.Status.SUCCESS -> {
                    //    showProgress(false)
                    viewModel.setCurrentStudentMarks(it.data!!)
                    setDefaultValues(it.data!!)

                }
                Resource.Status.ERROR -> {
                    //   showProgress(false)

                }
            }
        }






        viewModel.editMarksStatus.collect {
            when (it.status) {
                Resource.Status.LOADING -> {
                    //    showProgress(true)

                }
                Resource.Status.SUCCESS -> {
                    //     showProgress(false)

                }
                Resource.Status.ERROR -> {
                    showToastInfo("Error: ${it.exception?.message}")
                }
            }
        }


    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setOnClickListeners() {
        binding.submitBtn.setOnClickListener {
            val studentMarks = getStudentMarksObject()
            viewModel.setEvent(Event.SubmitClicked(studentMarks))

        }
    }

    private fun getStudentMarksObject(): StudentMarks {

        val studentMarks = viewModel.currentStudentMarks.value!!.toObject(StudentMarks::class.java)!!
        binding.apply {
            studentMarks.math = mathEdtTxt.text.toString()
            studentMarks.science = scienceEdtTxt.text.toString()
            studentMarks.english = englishEdtTxt.text.toString()
            studentMarks.kiswahili = kiswahiliEdtTxt.text.toString()
            studentMarks.sst_cre = sstCreEdtTxt.text.toString()


        }
        return studentMarks

    }


    private fun setDefaultValues(snapshot: DocumentSnapshot) {
        val studentMarks = snapshot.toObject(StudentMarks::class.java)!!
        binding.apply {
            nameTxtView.setText(studentMarks!!.fullName)
            mathEdtTxt.setText("" + studentMarks!!.math)
            scienceEdtTxt.setText("" + studentMarks!!.science)
            englishEdtTxt.setText("" + studentMarks!!.english)
            kiswahiliEdtTxt.setText("" + studentMarks!!.kiswahili)
            sstCreEdtTxt.setText("" + studentMarks!!.sst_cre)
            totalEdtTxt.setText(studentMarks.totalMarks.toString())
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
        data class SubmitClicked(val studentMarks: StudentMarks) : Event()
    }
}