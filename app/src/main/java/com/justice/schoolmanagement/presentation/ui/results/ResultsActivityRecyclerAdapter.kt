package com.justice.schoolmanagement.presentation.ui.results

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemResultsBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks

class ResultsActivityRecyclerAdapter(private val resultsFragment: ResultsFragment, options: FirestoreRecyclerOptions<StudentMarks?>) : FirestoreRecyclerAdapter<StudentMarks, ResultsActivityRecyclerAdapter.ViewHolder>(options) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: StudentMarks) {

        //////////////////////////////////
        holder.binding.apply {
            positionTxtView.text = "" + model.position
            nameTxtView.text = "" + model.name
            mathTxtView.text = "" + model.math
            scienceTxtView.text = "" + model.science
            englishTxtView.text = "" + model.english
            kiswahiliTxtView.text = "" + model.kiswahili
            sstCreTxtView.text = "" + model.sst_cre
            classGradeTxtView.text = "Class: " + model.classGrade
            val totalMarks = model.math + model.science + model.english + model.kiswahili + model.sst_cre
            totalMarkTxtView.text = "" + totalMarks
            editTxtView.setOnClickListener {
                ApplicationClass.documentSnapshot = snapshots.getSnapshot(position)


              resultsFragment.findNavController().navigate(R.id.action_resultsFragment_to_resultsEditFragment)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_results, parent, false)

        val binding = ItemResultsBinding.bind(view)
        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: ItemResultsBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}