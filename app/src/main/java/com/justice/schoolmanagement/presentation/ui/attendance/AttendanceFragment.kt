package com.justice.schoolmanagement.presentation.ui.attendance

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentAttendanceBinding
import com.justice.schoolmanagement.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AttendanceFragment : Fragment(R.layout.fragment_attendance),
    DatePickerDialog.OnDateSetListener {

    private val TAG = "AttendanceFragment"

    lateinit var binding: FragmentAttendanceBinding

    lateinit var attendanceAdapter: AttendanceAdapter

    @Inject
    lateinit var requestManager: RequestManager
    private val viewModel: AttendanceViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAttendanceBinding.bind(view)
        initProgressBar()
        initRecyclerView()
        setOnClickListeners()

        subScribeToObservers()


    }

    private fun subScribeToObservers() {

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getCurrentDate.collect {
                    Log.d(TAG, "subScribeToObservers: getCurrentDate status:${it.status}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            updateLabel(it.data!!)
                            viewModel.setEvent(Event.FetchAttendance(it.data!!))
                        }

                    }
                }

            }

            launch {
                viewModel.attendanceEvents.collect {
                    when (it) {
                        is Event.DateClicked -> {
                            showDateDialog()
                        }
                        is Event.SetLocationClicked -> {
                            goToSetLocationScreen()
                        }
                    }
                }

            }
            launch {
                viewModel.dateChoosenStatus.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            updateLabel(it.data!!)
                            viewModel.setEvent(Event.FetchAttendance(it.data!!))
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showToastInfo("Error: ${it.exception?.message}")
                        }
                    }
                }

            }

            launch {
                viewModel.fetchAttendanceStatus.collect {

                    Log.d(TAG, "subScribeToObservers: fetchAttendanceStatus status:${it.status}")

                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)

                            attendanceAdapter.submitList(it.data)

                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showToastInfo("Error: ${it.exception?.message}")

                        }
                        Resource.Status.EMPTY -> {
                            showProgress(false)
                            showToastInfo("No Records in database!!")

                        }
                    }
                }

            }
        }


    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun showDateDialog() {
        val myCalendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(), this, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
            myCalendar[Calendar.DAY_OF_MONTH]
        ).show()

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val choosenCalender = Calendar.getInstance()

        choosenCalender[Calendar.YEAR] = year
        choosenCalender[Calendar.MONTH] = month
        choosenCalender[Calendar.DAY_OF_MONTH] = dayOfMonth
        //month usually starts from 0
        viewModel.setEvent(Event.DateChoosen(choosenCalender.time))

    }

    private fun goToSetLocationScreen() {
        findNavController().navigate(R.id.action_attendanceFragment_to_setLocationFragment)

    }

    private fun setOnClickListeners() {


        binding.dateBtn.setOnClickListener {
            viewModel.setEvent(Event.DateClicked)
        }
        binding.setLocationBtn.setOnClickListener {
            viewModel.setEvent(Event.SetLocationClicked)
        }


    }

    private fun updateLabel(choosenDate: String) {


        binding.currentDateTxtView.text = choosenDate

    }


    private fun initRecyclerView() {


        attendanceAdapter = AttendanceAdapter(requestManager)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceAdapter
        }


    }


    /////////////////////PROGRESS_BAR////////////////////////////
    lateinit var dialog: AlertDialog

    fun showProgress(show: Boolean) {

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
        object DateClicked : Event()
        object SetLocationClicked : Event()
        data class DateChoosen(val choosenDate: Date) : Event()
        data class FetchAttendance(val choosenDate: String) : Event()
    }


}