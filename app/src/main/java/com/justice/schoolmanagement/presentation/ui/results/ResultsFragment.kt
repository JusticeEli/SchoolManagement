package com.justice.schoolmanagement.presentation.ui.results

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.alldata.AllData
import com.justice.schoolmanagement.databinding.FragmentResultsBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.presentation.utils.Constants
import java.util.*

class ResultsFragment : Fragment(R.layout.fragment_results) {
    private var counter = 1
    private var resultsActivityRecyclerAdapter: ResultsActivityRecyclerAdapter? = null

    lateinit var binding: FragmentResultsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResultsBinding.bind(view)
        setUpRecyclerView()


        setOnClickListeners()


    }

    private fun setOnClickListeners() {
        binding.searchEdtTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val list: MutableList<StudentMarks> = ArrayList()
                if (binding.searchEdtTxt.getText().toString().isEmpty()) {
                } else {
                    for (studentMarks in AllData.studentMarksList) {
                        if (studentMarks.name.toLowerCase().contains(binding.searchEdtTxt.getText().toString().toLowerCase())) {
                            if (!list.contains(studentMarks)) {
                                list.add(studentMarks)
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }


    private fun setUpRecyclerView() {
        val query = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_STUDENTS_MARKS).orderBy("totalMarks", Query.Direction.DESCENDING)
        val recyclerOptions = FirestoreRecyclerOptions.Builder<StudentMarks>().setLifecycleOwner(viewLifecycleOwner).setQuery(query) { snapshot ->
            val studentMarks = snapshot.toObject(StudentMarks::class.java)
            studentMarks!!.position = counter++
            studentMarks.id = snapshot.id
            studentMarks
        }.build()
        resultsActivityRecyclerAdapter = ResultsActivityRecyclerAdapter(this, recyclerOptions)
        binding.apply {
            recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
            recyclerView.setAdapter(resultsActivityRecyclerAdapter)


        }
    }




    private fun sortStudentMarks() {
        var x = 1
        Collections.sort(AllData.studentMarksList)
        for (studentMarks in AllData.studentMarksList) {
            studentMarks.position = x++
        }
    }

    override fun onResume() {
        super.onResume()
        sortStudentMarks()
    }
}