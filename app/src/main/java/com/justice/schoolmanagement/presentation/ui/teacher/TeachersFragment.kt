package com.justice.schoolmanagement.presentation.ui.teacher

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentTeachersBinding
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty

class TeachersFragment : Fragment(R.layout.fragment_teachers) {
    private var teachersActivityRecyclerAdapter: TeachersActivityRecyclerAdapter? = null
    lateinit var binding: FragmentTeachersBinding
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTeachersBinding.bind(view)
navController=findNavController()
        setUpRecyclerViewAdapter();
        setSwipeListenerForItems()

    }

    private fun setUpRecyclerViewAdapter() {
        val query: Query = firebaseFirestore.collection(Constants.COLLECTION_TEACHERS)
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<TeacherData>().setQuery(query) { snapshot ->
            val teacherData = snapshot.toObject(TeacherData::class.java)
            teacherData!!.id = snapshot.id
            teacherData
        }.setLifecycleOwner(viewLifecycleOwner).build()
        teachersActivityRecyclerAdapter = TeachersActivityRecyclerAdapter(this, firestoreRecyclerOptions)
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.setAdapter(teachersActivityRecyclerAdapter)
    }

    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                teachersActivityRecyclerAdapter!!.deleteTeacherDataFromDatabase(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)


        inflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu.findItem(R.id.searchItem)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }
    /////////////////////PROGRESS_BAR////////////////////////////
         fun showProgress(show: Boolean) {
            if (show) {
             Toasty.info(requireContext(),"loading...")
            } else {
                Toasty.info(requireContext(),"finished loading")
            }
        }
    }
