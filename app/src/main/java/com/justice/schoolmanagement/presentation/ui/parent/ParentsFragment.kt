package com.justice.schoolmanagement.presentation.ui.parent

import android.os.Bundle
import android.view.View
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
import com.justice.schoolmanagement.databinding.FragmentParentsBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty

class ParentsFragment : Fragment(R.layout.fragment_parents) {
    private var parentsActivityRecyclerAdapter: ParentsActivityRecyclerAdapter? = null


    private val firebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var binding: FragmentParentsBinding
    lateinit var navController:NavController
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentParentsBinding.bind(view)
navController=findNavController()
        initRecyclerViewAdapter()
        setOnClickListeners()
        setSwipeListenerForItems()

    }

    private fun initRecyclerViewAdapter() {
        val query: Query = firebaseFirestore.collection(Constants.COLLECTION_PARENTS)
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<ParentData>().setQuery(query) { snapshot ->
            val parentData = snapshot.toObject(ParentData::class.java)
            parentData!!.id = snapshot.id
            parentData!!
        }.setLifecycleOwner(viewLifecycleOwner).build()


        parentsActivityRecyclerAdapter = ParentsActivityRecyclerAdapter(this, firestoreRecyclerOptions)
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
       binding. recyclerView.setAdapter(parentsActivityRecyclerAdapter)

    }

    private fun setOnClickListeners() {
      binding.  addParentBtn.setOnClickListener(View.OnClickListener {

          ApplicationClass.documentSnapshot=null

          navController.navigate(R.id.action_parentsFragment_to_addParentFragment)
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
             Toasty.info(requireContext(),"loading...")
            } else {
                Toasty.info(requireContext(),"finished loading")
            }
        }
    }
