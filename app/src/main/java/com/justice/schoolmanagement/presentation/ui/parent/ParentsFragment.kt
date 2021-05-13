package com.justice.schoolmanagement.presentation.ui.parent

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
import com.justice.schoolmanagement.databinding.FragmentParentsBinding
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import com.justice.schoolmanagement.utils.Resource
import com.justice.schoolmanagement.utils.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ParentsFragment : Fragment(R.layout.fragment_parents) {

    private val TAG = "ParentsFragment"

    @Inject
    lateinit var requestManager: RequestManager
    lateinit var binding: FragmentParentsBinding
    lateinit var navController: NavController
    lateinit var searchView: SearchView
    lateinit var adapter: ParentsAdapter

    private val viewModel: ParentViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding = FragmentParentsBinding.bind(view)
        navController = findNavController()
        initProgressBar()
        setOnClickListeners()
        initRecyclerViewAdapter()
        setSwipeListenerForItems()
        setHasOptionsMenu(true)
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getParents.collect {
                Log.d(TAG, "subscribeToObservers: getParents:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)

                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        viewModel.setCurrentParentsLiveData(it.data!!.documents)
                        adapter.submitList(it.data?.documents)
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showErrorToast(it.exception?.message ?: "Error Occurred")

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.parentQueryStatus.collect {
                Log.d(TAG, "subscribeToObservers: parentQueryStatus:${it.status.name}")

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
            viewModel.parentChannelEvents.collect {
                when (it) {
                    is Event.AddParent -> {
                        goToAddParentFragment()
                    }
                    is Event.ParentDelete -> {
                        deleteFromDatabase(it.parentSnapshot)
                    }

                    is Event.ParentSwiped -> {
                        deleteFromDatabase(it.parentSnapshot)
                    }
                    is Event.ParentClicked -> {
                        val parent = it.parentSnapshot.toObject(ParentData::class.java)!!
                        Log.d(TAG, "subscribeToObservers: ")
                        navController.navigate(ParentsFragmentDirections.actionParentsFragmentToParentDetailsFragment(parent))
                    }
                    is Event.ParentEdit -> {
                        val parent = it.parentSnapshot.toObject(ParentData::class.java)!!
                        navController.navigate(ParentsFragmentDirections.actionParentsFragmentToEditParentFragment(parent))
                    }

                }

            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.deleteParentStatus.collect {
                Log.d(TAG, "subscribeToObservers: deleteParentStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {

                    }
                    Resource.Status.SUCCESS -> {

                    }
                    Resource.Status.ERROR -> {
                        showErrorToast("Error: ${it.exception?.message}")

                    }
                }
            }
        }
    }

    private fun goToAddParentFragment() {
        navController.navigate(ParentsFragmentDirections.actionParentsFragmentToAddParentFragment(null))
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_blog, menu)

        val searchItem = menu.findItem(R.id.searchItem)
        searchView = searchItem.actionView as SearchView


        searchView.onQueryTextChanged { query ->
            Log.d(TAG, "onCreateOptionsMenu: query:$query")
            viewModel.setEvent(Event.ParentQuery(query))

        }
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun initRecyclerViewAdapter() {

        adapter = ParentsAdapter(requestManager, onEditClicked = { onEditClicked(it) }, onParentClicked = { onParentClicked(it) }, onParentDelete = { onParentDelete(it) })
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.setAdapter(adapter)

    }

    private fun onParentDelete(it: DocumentSnapshot) {
        viewModel.setEvent(Event.ParentDelete(it))
    }

    private fun onParentClicked(it: DocumentSnapshot) {
        viewModel.setEvent(Event.ParentClicked(it))
    }

    private fun onEditClicked(it: DocumentSnapshot) {
        viewModel.setEvent(Event.ParentEdit(it))

    }

    private fun setOnClickListeners() {
        binding.addParentBtn.setOnClickListener {
            viewModel.setEvent(Event.AddParent)
        }
    }

    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val snapshot = adapter.currentList[viewHolder.bindingAdapterPosition]
                viewModel.setEvent(Event.ParentSwiped(snapshot))
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
    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

    fun deleteFromDatabase(snapshot: DocumentSnapshot) {
        MaterialAlertDialogBuilder(requireContext()).setBackground(requireActivity().getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which ->
            val position = adapter.currentList.indexOf(snapshot)
            adapter.notifyItemChanged(position)
        }.setPositiveButton("yes") { dialog, which ->

            viewModel.setEvent(Event.ParentConfirmDelete(snapshot))
        }.show()
    }



    private fun showSuccessToast(message: String) {
        Toasty.success(requireContext(), message).show()
    }

    private fun showInfoToast(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun showErrorToast(message: String) {
        Toasty.error(requireContext(), message).show()

    }



    sealed class Event {
        data class ParentClicked(val parentSnapshot: DocumentSnapshot) : Event()
        data class ParentEdit(val parentSnapshot: DocumentSnapshot) : Event()
        data class ParentDelete(val parentSnapshot: DocumentSnapshot) : Event()
        data class ParentConfirmDelete(val parentSnapshot: DocumentSnapshot) : Event()
        data class ParentSwiped(val parentSnapshot: DocumentSnapshot) : Event()
        data class ParentQuery(val query: String) : Event()
        object AddParent : Event()
    }

    companion object{
        const val PARENT_ARGS = "parent"
    }
}
