package com.justice.schoolmanagement.presentation.ui.class_

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentChoosenClassBinding
import com.justice.schoolmanagement.presentation.ui.results.ResultsAdapter
import com.justice.schoolmanagement.presentation.ui.results.ResultsFragment
import com.justice.schoolmanagement.presentation.ui.student.StudentAdapter
import com.justice.schoolmanagement.presentation.ui.student.StudentsFragment
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_choosen_class.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ChoosenClassFragment : Fragment(R.layout.fragment_choosen_class) {

    private val TAG = "ChoosenClassFragment"

    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var studentsAdapter: StudentAdapter

    @Inject
    lateinit var requestManager: RequestManager


    private lateinit var binding: FragmentChoosenClassBinding
    private val navArgs: ChoosenClassFragmentArgs by navArgs()

    private lateinit var navController: NavController
    private val viewModel: ChoosenClassViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChoosenClassBinding.bind(view)
        Log.d(TAG, "onViewCreated: classGrade:${navArgs.classGrade}")
        navController = findNavController()
        initRecyclerViews()

        setDefaultValues()
        setOnClickListeners()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            subScribeToObserversStudents()
            subScribeToObserversResults()
        }

    }
    private suspend fun subScribeToObserversStudents() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getStudents.collect {
                Log.d(TAG, "subScribeToObserversStudents:getStudents:${it.status.name} ")

                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        viewModel.setCurrentStudentsList(it.data?.documents)
                        studentsAdapter.submitList(it.data?.documents)
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)

                    }
                }
            }

        }


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.studentsEvents.collect {
                when (it) {
                    is StudentsFragment.Event.StudentClicked -> {
                        val student = it.parentSnapshot.toObject(StudentData::class.java)
                        Log.d(TAG, "subScribeToObservers: student:$student")
                        navController.navigate(ChoosenClassFragmentDirections.actionChoosenClassFragmentToStudentDetailsFragment(student!!))

                    }
                    is StudentsFragment.Event.StudentEdit -> {
                        val student = it.parentSnapshot.toObject(StudentData::class.java)
                        navController.navigate(ChoosenClassFragmentDirections.actionChoosenClassFragmentToEditStudentFragment(student!!))

                    }
                    is StudentsFragment.Event.StudentDelete -> {
                        deleteStudentFromDatabase(it.parentSnapshot)

                    }
                    is StudentsFragment.Event.StudentSwiped -> {
                        deleteStudentFromDatabase(it.parentSnapshot)
                    }

                    StudentsFragment.Event.AddStudent -> {
                        findNavController().navigate(R.id.action_studentsFragment_to_addStudentFragment)
                    }
                }
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {

            viewModel.deleteStudentStatus.collect {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        showToastInfo("Success deleting student")
                    }
                    Resource.Status.ERROR -> {
                        showToastInfo("Error: ${it.exception?.message}")
                    }
                }
            }
        }


    }

    private suspend fun subScribeToObserversResults() {

        viewModel.getAllMarks.collect {
            Log.d(TAG, "subScribeToObserversResults: getAllMarks:${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {
                    showProgress(true)

                }
                Resource.Status.SUCCESS -> {
                    showProgress(false)
                    Log.d(TAG, "subScribeToObserversResults:getAllMarks: size:${it.data?.size}")
                    resultsAdapter.submitList(it.data)

                }
                Resource.Status.EMPTY -> {
                    showProgress(false)
                    showToastInfo("Database is Empty")

                }
                Resource.Status.ERROR -> {
                    showProgress(false)
                    showToastInfo("Error: ${it.exception?.message}")
                    Log.d(TAG, "subScribeToObserversResults:Error: ${it.exception?.message} ")
                    it.exception?.printStackTrace()

                }
            }
        }

        viewModel.resultEvents.collect {
            when (it) {
                is ResultsFragment.Event.EditClicked -> {
                    goToEditScreen(it.snapshot)
                }
            }
        }
    }

    private fun initRecyclerViews() {
        initResultsAdapter()
        initStudentAdapter()
    }


    private fun goToEditScreen(snapshot: DocumentSnapshot) {
        val studentMarks = snapshot.toObject(StudentMarks::class.java)!!
        findNavController().navigate(ChoosenClassFragmentDirections.actionChoosenClassFragmentToResultsEditFragment(studentMarks))
    }


    private fun initResultsAdapter() {
        resultsAdapter = ResultsAdapter { onEditClickedResults(it) }
        binding.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultsAdapter
        }
    }

    private fun onEditClickedResults(it: DocumentSnapshot) {
        viewModel.setEventResults(Event.EditClicked(it))
    }


    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun initStudentAdapter() {
        studentsAdapter = StudentAdapter(requestManager, { onEditClicked(it) }, { onStudentClicked(it) }, { onStudentDelete(it) })
        binding.studentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = studentsAdapter
        }
    }

    private fun onStudentDelete(it: DocumentSnapshot) {
        viewModel.setEventStudent(Event.StudentDelete(it))
    }

    private fun onStudentClicked(it: DocumentSnapshot) {
        Log.d(TAG, "onStudentClicked: ")
        viewModel.setEventStudent(Event.StudentClicked(it))
    }

    private fun onEditClicked(it: DocumentSnapshot) {
        viewModel.setEventStudent(Event.StudentEdit(it))

    }

    fun deleteStudentFromDatabase(snapshot: DocumentSnapshot) {
        MaterialAlertDialogBuilder(requireContext()).setBackground(requireActivity().getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which ->
            val position = studentsAdapter.currentList.indexOf(snapshot)
            studentsAdapter.notifyItemChanged(position)
        }.setPositiveButton("yes") { dialog, which ->
            viewModel.setEventStudent(Event.StudentDeleteConfirmed(snapshot))
        }.show()
    }

    private fun setDefaultValues() {
        headerTxtView.setText("Class ${navArgs.classGrade}")
    }


    private fun setOnClickListeners() {
        setOnClickListenerForSearchEdtTxt()
        binding.studentBtn.setOnClickListener {
            studentsRecyclerView.setVisibility(View.VISIBLE)
            resultsRecyclerView.setVisibility(View.GONE)
        }
        binding.resultsBtn.setOnClickListener {
            studentsRecyclerView.setVisibility(View.GONE)
            resultsRecyclerView.setVisibility(View.VISIBLE)
        }
    }

    private fun setOnClickListenerForSearchEdtTxt() {}

    /////////////////////PROGRESS_BAR////////////////////////////
    fun showProgress(show: Boolean) {
       /* if (show) {
            Toasty.info(requireContext(), "loading...").show()
        } else {
            Toasty.info(requireContext(), "finished loading").show()
        }*/
    }


    sealed class Event {
        data class StudentClicked(val parentSnapshot: DocumentSnapshot) : Event()
        data class StudentEdit(val parentSnapshot: DocumentSnapshot) : Event()
        data class StudentDelete(val parentSnapshot: DocumentSnapshot) : Event()
        data class StudentDeleteConfirmed(val parentSnapshot: DocumentSnapshot) : Event()
        data class EditClicked(val snapshot: DocumentSnapshot) : Event()

    }
}



