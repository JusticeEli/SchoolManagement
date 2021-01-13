package com.justice.schoolmanagement.presentation.ui.parent

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentParentsBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty

class ParentsFragment : Fragment(R.layout.fragment_parents) {
    private var parentsActivityRecyclerAdapter: ParentsActivityRecyclerAdapter? = null
    lateinit var parentFilterAdapter: ParentFilterAdapter
    lateinit var searchView: SearchView
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var binding: FragmentParentsBinding
    lateinit var navController: NavController
    lateinit var originalList: List<DocumentSnapshot>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentParentsBinding.bind(view)
        navController = findNavController()
        initRecyclerViewAdapter()
        setOnClickListeners()
        setSwipeListenerForItems()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_blog, menu)

        val searchItem = menu.findItem(R.id.searchItem)
        searchView = searchItem.actionView as SearchView


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {

                Log.d(TAG, "onQueryTextChange: Text: ${s}")

                if (s.isNullOrEmpty()) {
                    startUsingRealTimeAdapter()
                    Log.d(TAG, "onQueryTextChange: startUsingRealTimeAdapter")

                } else {
                    startUsingParentFilter(s.trim())
                    Log.d(TAG, "onQueryTextChange:  startUsingParentFilter ")

                }
                return true

            }
        })



        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun startUsingRealTimeAdapter() {
        binding.recyclerView.adapter = parentsActivityRecyclerAdapter
    }

    private fun startUsingParentFilter(string: String) {
        originalList = mutableListOf<DocumentSnapshot>()
        (originalList as MutableList<DocumentSnapshot>)?.clear()
        parentsActivityRecyclerAdapter?.snapshots?.forEachIndexed { index, _ ->
            (originalList as MutableList<DocumentSnapshot>).add(parentsActivityRecyclerAdapter!!.snapshots.getSnapshot(index))
        }
        parentFilterAdapter.submitList(originalList)
        binding.recyclerView.adapter = parentFilterAdapter

        parentFilterAdapter.getFilter().filter(string)

    }

    private fun initRecyclerViewAdapter() {
        parentFilterAdapter = ParentFilterAdapter(this)
        val query: Query = firebaseFirestore.collection(Constants.COLLECTION_PARENTS)
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<ParentData>().setQuery(query) { snapshot ->
            val parentData = snapshot.toObject(ParentData::class.java)
            parentData!!.id = snapshot.id
            parentData!!
        }.setLifecycleOwner(viewLifecycleOwner).build()


        parentsActivityRecyclerAdapter = ParentsActivityRecyclerAdapter(this, firestoreRecyclerOptions)
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.setAdapter(parentsActivityRecyclerAdapter)

    }

    private fun setOnClickListeners() {
        binding.addParentBtn.setOnClickListener(View.OnClickListener {

            ApplicationClass.documentSnapshot = null

            navController.navigate(ParentsFragmentDirections.actionParentsFragmentToAddParentFragment(null, null))
        })
    }

    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                parentsActivityRecyclerAdapter?.deleteFromDatabase(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    fun showProgress(show: Boolean) {
        if (show) {
            Toasty.info(requireContext(), "loading...")
        } else {
            Toasty.info(requireContext(), "finished loading")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

    companion object {
        private const val TAG = "ParentsFragment"
    }
}
