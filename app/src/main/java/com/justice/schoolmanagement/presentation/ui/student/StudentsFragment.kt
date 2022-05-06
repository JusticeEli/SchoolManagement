package com.justice.schoolmanagement.presentation.ui.student

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentStudentsBinding
import com.justice.schoolmanagement.presentation.MainActivity
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.utils.Resource
import com.justice.schoolmanagement.utils.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class StudentsFragment : Fragment(R.layout.fragment_students) {

    private val TAG = "StudentsFragment"

    @Inject
    lateinit var requestManager: RequestManager
    lateinit var adapter: StudentAdapter
    lateinit var binding: FragmentStudentsBinding
    lateinit var navController: NavController
    lateinit var searchView: SearchView
    private val viewModel: StudentsViewModel by viewModels()

    companion object {
        const val STUDENT_ARGS = "studentData"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStudentsBinding.bind(view)
        navController = findNavController()
        initProgressBar()
        setHasOptionsMenu(true)
        initRecyclerViewAdapter()
        setOnClickListeners()
        setSwipeListenerForItems()
        subScribeToObservers()

    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getStudents.collect {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        viewModel.setCurrentListLiveData(it.data?.documents)
                        adapter.submitList(it.data?.documents)
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
                    is Event.StudentClicked -> {
                        val student = it.parentSnapshot.toObject(StudentData::class.java)
                        Log.d(TAG, "subScribeToObservers: student:$student")
                         navController.navigate(StudentsFragmentDirections.actionStudentsFragmentToStudentDetailsFragment(student!!))

                    }
                    is Event.StudentEdit -> {
                        val student = it.parentSnapshot.toObject(StudentData::class.java)
                        navController.navigate(StudentsFragmentDirections.actionStudentsFragmentToEditStudentFragment(student!!))

                    }
                    is Event.StudentDelete -> {
                        deleteStudentFromDatabase(it.parentSnapshot)

                    }
                    is Event.StudentSwiped -> {
                        deleteStudentFromDatabase(it.parentSnapshot)
                    }

                    Event.AddStudent -> {
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


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.studentQueryStatus.collect {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)

                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        adapter.submitList(it.data)

                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        Log.d(TAG, "subScribeToObservers: Error: ${it.exception?.message}")
                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)
                        Log.d(TAG, "subScribeToObservers: empty query has been passed")
                        adapter.submitList(viewModel.currentListLiveData.value)

                    }
                }
            }
        }
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_blog, menu)

        val searchItem = menu.findItem(R.id.searchItem)
        searchView = searchItem.actionView as SearchView


        searchView.onQueryTextChanged { query ->
            Log.d(TAG, "onCreateOptionsMenu: query:$query")
            viewModel.setEvent(Event.StudentQuery(query))
        }



        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (requireActivity() as MainActivity).onOptionsItemSelected(item)
    }

    private fun initRecyclerViewAdapter() {
        adapter = StudentAdapter(requestManager, { onEditClicked(it) }, { onStudentClicked(it) }, { onStudentDelete(it) })
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.setAdapter(adapter)
    }

    private fun onStudentDelete(it: DocumentSnapshot) {
        viewModel.setEvent(Event.StudentDelete(it))
    }

    private fun onStudentClicked(it: DocumentSnapshot) {
        Log.d(TAG, "onStudentClicked: ")
        viewModel.setEvent(Event.StudentClicked(it))
    }

    private fun onEditClicked(it: DocumentSnapshot) {
        viewModel.setEvent(Event.StudentEdit(it))

    }

    private fun setOnClickListeners() {
        binding.addStudentBtn.setOnClickListener {
            viewModel.setEvent(Event.AddStudent)
        }
    }

    fun deleteStudentFromDatabase(snapshot: DocumentSnapshot) {
        MaterialAlertDialogBuilder(requireContext()).setBackground(requireActivity().getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which ->
            val position = adapter.currentList.indexOf(snapshot)
            adapter.notifyItemChanged(position)
        }.setPositiveButton("yes") { dialog, which ->
            viewModel.setEvent(Event.StudentDeleteConfirmed(snapshot))
        }.show()
    }

    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val snapshot = adapter.currentList.get(viewHolder.bindingAdapterPosition)
                viewModel.setEvent(Event.StudentSwiped(snapshot))
            }
        }).attachToRecyclerView(binding.recyclerView)
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
        data class StudentClicked(val parentSnapshot: DocumentSnapshot) : Event()
        data class StudentEdit(val parentSnapshot: DocumentSnapshot) : Event()
        data class StudentDelete(val parentSnapshot: DocumentSnapshot) : Event()
        data class StudentDeleteConfirmed(val parentSnapshot: DocumentSnapshot) : Event()
        data class StudentSwiped(val parentSnapshot: DocumentSnapshot) : Event()
        data class StudentQuery(val query: String) : Event()
        object AddStudent : Event()
    }


}
