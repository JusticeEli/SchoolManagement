package com.justice.schoolmanagement.presentation.ui.class_

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemResultsBinding
import com.justice.schoolmanagement.presentation.SchoolApplication
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks

class ChoosenClassActivityResultsRecyclerAdapter(private val choosenClassFragment: ChoosenClassFragment, options: FirestoreRecyclerOptions<StudentMarks?>) : FirestoreRecyclerAdapter<StudentMarks, ChoosenClassActivityResultsRecyclerAdapter.ViewHolder>(options) {
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


                SchoolApplication.documentSnapshot = snapshots.getSnapshot(position)
                choosenClassFragment.navController.navigate(R.id.action_choosenClassFragment_to_resultsEditFragment)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_results, parent, false)

        val binding:ItemResultsBinding= ItemResultsBinding.bind(view)
        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: ItemResultsBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}