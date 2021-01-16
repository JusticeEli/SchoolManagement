package com.justice.schoolmanagement.presentation.ui.blog

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentBlogBinding
import com.justice.schoolmanagement.presentation.ui.blog.model.Blog
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty

class BlogFragment : Fragment(R.layout.fragment_blog) {
    private var adapter: BlogRecyclerAdapter? = null

    lateinit var binding: FragmentBlogBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBlogBinding.bind(view)

        setUpRecyclerView()
        setOnClickListeners()
        setSwipeToDelete()

    }

    private fun setSwipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                MaterialAlertDialogBuilder(requireContext()).setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> adapter?.notifyItemChanged(viewHolder.adapterPosition) }.setPositiveButton("yes") { dialog, which -> deleteBlog(adapter!!.getBlogReferenceByPosition(viewHolder.adapterPosition)) }.show()
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


            findNavController().navigate(R.id.action_blogFragment_to_addBlogFragment)
        }
    }

    private fun setUpRecyclerView() {
        val query = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.BLOGS).orderBy("date", Query.Direction.DESCENDING)
        val recyclerOptions = FirestoreRecyclerOptions.Builder<Blog>().setLifecycleOwner(viewLifecycleOwner).setQuery(query) { snapshot ->
            val blog = snapshot.toObject(Blog::class.java)
            blog!!.id = snapshot.id
            blog
        }.build()
        adapter = BlogRecyclerAdapter(this, recyclerOptions)
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.setAdapter(adapter)
    }

}