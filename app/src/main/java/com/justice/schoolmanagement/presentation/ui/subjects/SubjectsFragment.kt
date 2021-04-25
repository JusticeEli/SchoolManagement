package com.justice.schoolmanagement.presentation.ui.subjects

import android.R.layout
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.alldata.AllData
import com.justice.schoolmanagement.databinding.FragmentSubjectsBinding
import com.justice.schoolmanagement.presentation.SchoolApplication
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubjectsFragment : Fragment(R.layout.fragment_subjects) {

    private val TAG = "SubjectsFragment"

    lateinit var binding: FragmentSubjectsBinding
    private val viewModel: SubjectsViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubjectsBinding.bind(view)
        initProgressBar()
        setSpinnerValues()
        setOnClickListeners()

        subScribeToObserves()


    }

    private fun subScribeToObserves() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getAllTeachers.collect {
                    Log.d(TAG, "subScribeToObserves:getAllTeachers: ${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            viewModel.setCurrentTeachersLiveData(it.data!!)
                            viewModel.setEvent(Event.SubjectClicked(0))

                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                        }
                    }
                }
            }


            launch {
                viewModel.clickedResults.collect {
                    Log.d(TAG, "subScribeToObserves: Results after click: ${it.data?.size}")
                    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, it.data!!)
                    binding.listView.setAdapter(arrayAdapter)
                }
            }
        }


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
                setSpinnerValues()
                setOnClickListeners()

            } else {
                Toast.makeText(requireContext(), "Error: " + task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }

    private fun setOnClickListeners() {
        var check = 0
        binding.spinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                if (++check > 1) {
                    viewModel.setEvent(Event.SubjectClicked(position))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }


    private fun setSpinnerValues() {
        val subjects = requireActivity().resources.getStringArray(R.array.subjects)
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_dropdown_item_1line, subjects)
        binding.spinner.setAdapter(arrayAdapter)
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
        data class SubjectClicked(val position: Int) : Event()
    }
}