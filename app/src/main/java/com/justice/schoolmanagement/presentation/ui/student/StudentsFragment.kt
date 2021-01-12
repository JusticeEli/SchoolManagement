package com.justice.schoolmanagement.presentation.ui.student

import android.content.Intent
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
import com.justice.schoolmanagement.databinding.FragmentStudentsBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.utils.Constants
import com.justice.schoolmanagement.student.AddStudentActivity
import es.dmoral.toasty.Toasty

class StudentsFragment : Fragment(R.layout.fragment_students) {
    private var studentsActivityRecyclerAdapter: StudentsActivityRecyclerAdapter? = null

    lateinit var binding: FragmentStudentsBinding
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStudentsBinding.bind(view)
        navController = findNavController()
        initRecyclerViewAdapter();
        setOnClickListeners()
        setSwipeListenerForItems()

    }

    private fun initRecyclerViewAdapter() {
        val query: Query = firebaseFirestore.collection(Constants.COLLECTION_STUDENTS)
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<StudentData>().setQuery(query) { snapshot ->
            val studentData = snapshot.toObject(StudentData::class.java)
            studentData!!.id = snapshot.id
            studentData
        }.setLifecycleOwner(viewLifecycleOwner).build()


        studentsActivityRecyclerAdapter = StudentsActivityRecyclerAdapter(this, firestoreRecyclerOptions)
        binding.recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.recyclerView.setAdapter(studentsActivityRecyclerAdapter)
    }

    private fun setOnClickListeners() {
        binding.addStudentBtn.setOnClickListener(View.OnClickListener {
            val intent = Intent(requireContext(), AddStudentActivity::class.java)
            startActivity(intent)
        })
    }

    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                studentsActivityRecyclerAdapter!!.deleteStudentFromDatabase(viewHolder.adapterPosition)
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
}
