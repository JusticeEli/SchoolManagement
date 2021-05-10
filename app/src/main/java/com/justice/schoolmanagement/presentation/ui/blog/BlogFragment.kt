package com.justice.schoolmanagement.presentation.ui.blog

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.edward.nyansapo.wrappers.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentBlogBinding
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

const val FIELD_DATE = "date"

class BlogFragment : Fragment(R.layout.fragment_blog) {
    private lateinit var adapter: BlogAdapter
    lateinit var binding: FragmentBlogBinding
    private val viewModel: BlogViewModel by viewModels()

    @Inject
    lateinit var requestManager: RequestManager
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBlogBinding.bind(view)
        setUpRecyclerView()
        setSwipeToDelete()
        setOnClickListeners()
        subScribeToObservers()
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun subScribeToObservers() {

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getBlogs.collect {
                when (it.status) {
                    Resource.Status.LOADING -> {

                    }
                    Resource.Status.EMPTY -> {
                        showToastInfo("Database Is Empty,Please Add A Blog")

                    }
                    Resource.Status.SUCCESS -> {
                        adapter.submitList(it.data)

                    }
                    Resource.Status.ERROR -> {
                        showToastInfo("Error: ${it.exception?.message}")

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.blogEvents.collect {
                when (it) {
                    is Event.AddClicked -> {
                        goToAddBlogScreen()
                    } is Event.SwipedBlog -> {
                        swipeInit(it.snapshot)
                    }
                }
            }
        }
    }

    private fun swipeInit(snapshot: DocumentSnapshot) {
        MaterialAlertDialogBuilder(requireContext()).setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which ->
            adapter.notifyItemChanged(adapter.currentList.indexOf(snapshot))
        }.setPositiveButton("yes") { dialog, which ->
            viewModel.setEvent(Event.DeleteConfirmed(snapshot))

         }.show()
    }

    private fun goToAddBlogScreen() {
        findNavController().navigate(R.id.action_blogFragment_to_addBlogFragment)
    }

    private fun setSwipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val snapshot=adapter.currentList[viewHolder.bindingAdapterPosition]
                viewModel.setEvent(Event.SwipedBlog(snapshot))
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    private fun deleteBlog(blogReferenceByPosition: DocumentReference) {
        blogReferenceByPosition.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), "Deletion success", Toasty.LENGTH_SHORT).show()
            } else {
                Toasty.error(requireContext(), "Error: " + task.exception!!.message, Toasty.LENGTH_SHORT).show()
            }
        }
    }

    private fun setOnClickListeners() {
        binding.fob.setOnClickListener {
            viewModel.setEvent(Event.AddClicked)
        }
    }

    private fun setUpRecyclerView() {

        adapter = BlogAdapter(requestManager)
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.setAdapter(adapter)
    }

    sealed class Event {
        object AddClicked : Event()
        data class SwipedBlog(val snapshot: DocumentSnapshot): Event()
        data class DeleteConfirmed(val snapshot: DocumentSnapshot): Event()
    }

}