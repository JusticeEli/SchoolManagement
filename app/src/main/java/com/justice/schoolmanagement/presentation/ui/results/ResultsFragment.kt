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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentResultsBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ResultsFragment : Fragment(R.layout.fragment_results) {

    private val TAG = "ResultsFragment"

    private lateinit var resultsAdapter: ResultsAdapter

    lateinit var binding: FragmentResultsBinding
    private val viewModel: ResultsViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResultsBinding.bind(view)
        initProgressBar()
        setUpRecyclerView()

        subScribeToObservers()


    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {

            viewModel.getAllMarks.collect {
                Log.d(TAG, "subScribeToObservers:getAllMarks:${it.status.name} ")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)

                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        Log.d(TAG, "subScribeToObservers: size:${it.data?.size}")

                        val data =
                            it.data?.sortedByDescending { computeTotalMarks(it.toObject(StudentMarks::class.java)!!) }
                        resultsAdapter.submitList(data)

                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)
                        showToastInfo("Database is Empty")

                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToastInfo("Error: ${it.exception?.message}")

                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.resultEvents.collect {
                when (it) {
                    is Event.EditClicked -> {
                        goToEditScreen(it.snapshot)
                    }
                }
            }

        }

    }

    private fun computeTotalMarks(studentMarks: StudentMarks): Int {
        studentMarks.totalMarks =
            studentMarks.math.toIntOrZero() + studentMarks.science.toIntOrZero() + studentMarks.english.toIntOrZero() + studentMarks.kiswahili.toIntOrZero() + studentMarks.sst_cre.toIntOrZero()
        return studentMarks.totalMarks
    }

    private fun goToEditScreen(snapshot: DocumentSnapshot) {
        val studentMarks = snapshot.toObject(StudentMarks::class.java)!!
        findNavController().navigate(
            ResultsFragmentDirections.actionResultsFragmentToResultsEditFragment(
                studentMarks
            )
        )
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun setUpRecyclerView() {
        resultsAdapter = ResultsAdapter { onEditClicked(it) }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultsAdapter
        }
    }

    private fun onEditClicked(it: DocumentSnapshot) {
        viewModel.setEvent(Event.EditClicked(it))
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
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
        data class EditClicked(val snapshot: DocumentSnapshot) : Event()
    }
}