package com.justice.schoolmanagement.presentation.ui.teacher

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
import com.justice.schoolmanagement.databinding.FragmentTeachersBinding
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.utils.Constants
import com.justice.schoolmanagement.utils.Resource
import com.justice.schoolmanagement.utils.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TeachersFragment : Fragment(R.layout.fragment_teachers) {

    private val TAG = "TeachersFragment"

    private lateinit var searchView: SearchView

    private lateinit var adapter: TeacherAdapter

    private lateinit var binding: FragmentTeachersBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var requestManager: RequestManager
    private val viewModel: TeachersViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding = FragmentTeachersBinding.bind(view)
        navController = findNavController()
        setHasOptionsMenu(true)
        initProgressBar()
        setUpRecyclerViewAdapter()
        setSwipeListenerForItems()
        subscribeToObservers2()


    }

    private fun subscribeToObservers() {
        Log.d(TAG, "subscribeToObservers: ")
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getTeachers.collect {
                Log.d(TAG, "subscribeToObservers: teachersFetchStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)

                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        viewModel.setCurrentTeacherListLiveData(it.data!!.documents)
                        adapter.submitList(it.data?.documents)
                        Log.d(TAG, "subscribeToObservers: size:${it.data.size()}")
                        val path = Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS
                        Log.d(TAG, "subscribeToObservers: path:$path")
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showErrorToast(it.exception?.message ?: "Error Occurred")

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.teacherQueryStatus.collect {
                Log.d(TAG, "subscribeToObservers: teacherQueryStatus:${it.status.name}")

                when (it.status) {
                    Resource.Status.LOADING -> {
                        //   showProgress(true)
                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)

                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        adapter.submitList(it.data)
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showErrorToast(it.exception?.message ?: "Error Occurred")

                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.teacherChannelEvents.collect {
                when (it) {

                    is Event.TeacherDelete -> {
                        deleteFromDatabase(it.snapshot)
                    }
                    is Event.TeacherClicked -> {
                        val teacherData = it.snapshot.toObject(TeacherData::class.java)!!
                        navController.navigate(TeachersFragmentDirections.actionTeachersFragmentToTeacherDetailsFragment(teacherData))
                    }
                    is Event.TeacherEdit -> {
                        val teacherData = it.snapshot.toObject(TeacherData::class.java)!!
                        navController.navigate(TeachersFragmentDirections.actionTeachersFragmentToEditTeacherFragment(teacherData))
                    }
                    is Event.TeacherSwiped -> {
                        deleteFromDatabase(it.snapshot)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.deleteTeacherStatus.collect {
                when (it.status) {
                    Resource.Status.LOADING -> {

                    }
                    Resource.Status.SUCCESS -> {

                    }
                    Resource.Status.ERROR -> {

                    }
                }
            }
        }
    }

    private fun subscribeToObservers2() {
        Log.d(TAG, "subscribeToObservers: ")
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getTeachers.collect {
                    Log.d(TAG, "subscribeToObservers: teachersFetchStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                        }
                        Resource.Status.EMPTY -> {
                            showProgress(false)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            viewModel.setCurrentTeacherListLiveData(it.data!!.documents)
                            adapter.submitList(it.data?.documents)
                            Log.d(TAG, "subscribeToObservers: size:${it.data.size()}")
                            val path = Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS
                            Log.d(TAG, "subscribeToObservers: path:$path")
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showErrorToast(it.exception?.message ?: "Error Occurred")

                        }
                    }
                }

            }

            launch {
                viewModel.teacherQueryStatus.collect {
                    Log.d(TAG, "subscribeToObservers: teacherQueryStatus:${it.status.name}")

                    when (it.status) {
                        Resource.Status.LOADING -> {
                            //   showProgress(true)
                        }
                        Resource.Status.EMPTY -> {
                            showProgress(false)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            adapter.submitList(it.data)
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showErrorToast(it.exception?.message ?: "Error Occurred")

                        }

                    }
                }

            }

            launch {
                viewModel.teacherChannelEvents.collect {
                    when (it) {

                        is Event.TeacherDelete -> {
                            deleteFromDatabase(it.snapshot)
                        }
                        is Event.TeacherClicked -> {
                            val teacherData = it.snapshot.toObject(TeacherData::class.java)!!
                            navController.navigate(TeachersFragmentDirections.actionTeachersFragmentToTeacherDetailsFragment(teacherData))
                        }
                        is Event.TeacherEdit -> {
                            val teacherData = it.snapshot.toObject(TeacherData::class.java)!!
                            navController.navigate(TeachersFragmentDirections.actionTeachersFragmentToEditTeacherFragment(teacherData))
                        }
                        is Event.TeacherSwiped -> {
                            deleteFromDatabase(it.snapshot)

                        }
                    }
                }

            }
            launch {

                viewModel.deleteTeacherStatus.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {

                        }
                        Resource.Status.ERROR -> {

                        }
                    }

                }
            }


        }


    }

    private fun showErrorToast(message: String) {
        Toasty.error(requireContext(), message).show()
    }

    fun deleteFromDatabase(snapshot: DocumentSnapshot) {
        MaterialAlertDialogBuilder(requireContext()).setBackground(requireActivity().getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which ->
            val position = adapter.currentList.indexOf(snapshot)
            adapter.notifyItemChanged(position)
        }.setPositiveButton("yes") { dialog, which ->
            viewModel.setEvent(Event.TeacherConfirmDelete(snapshot))
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_blog, menu)
        val searchItem = menu.findItem(R.id.searchItem)
        searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged { query ->
            Log.d(TAG, "onCreateOptionsMenu: query:$query")
            viewModel.setEvent(Event.TeacherQuery(query))

        }
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun setUpRecyclerViewAdapter() {
        adapter = TeacherAdapter(requestManager, onEditClicked = { onEditClicked(it) }, onTeacherClicked = { onTeacherClicked(it) }, onTeacherDelete = { onTeacherDelete(it) })
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.adapter = adapter
    }

    private fun onTeacherDelete(it: DocumentSnapshot) {
        viewModel.setEvent(Event.TeacherDelete(it))
    }

    private fun onTeacherClicked(it: DocumentSnapshot) {
        viewModel.setEvent(Event.TeacherClicked(it))
    }

    private fun onEditClicked(it: DocumentSnapshot) {
        viewModel.setEvent(Event.TeacherEdit(it))

    }

    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val snapshot = adapter.currentList[viewHolder.bindingAdapterPosition]
                viewModel.setEvent(Event.TeacherSwiped(snapshot))
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
        data class TeacherClicked(val snapshot: DocumentSnapshot) : Event()
        data class TeacherEdit(val snapshot: DocumentSnapshot) : Event()
        data class TeacherDelete(val snapshot: DocumentSnapshot) : Event()
        data class TeacherConfirmDelete(val snapshot: DocumentSnapshot) : Event()
        data class TeacherSwiped(val snapshot: DocumentSnapshot) : Event()
        data class TeacherQuery(val query: String) : Event()
    }

    companion object {
        const val TEACHER_ARGS = "teacherData"
    }
}
