package com.justice.schoolmanagement.presentation.ui.results

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemResultsBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks

class ResultsAdapter(private val onEditClicked: (DocumentSnapshot) -> Unit) : ListAdapter<DocumentSnapshot, ResultsAdapter.ViewHolder>(DIFF_UTIL) {

    private val TAG = "ResultsAdapter"

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(StudentMarks::class.java)
                val new = newItem.toObject(StudentMarks::class.java)
                return old == new
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position).toObject(StudentMarks::class.java)!!
        Log.d(TAG, "onBindViewHolder: model:$model")
        computeTotalMarks(model)
        holder.binding.apply {
            positionTxtView.text = "${position+1}"
            nameTxtView.text = "" + model.fullName
            mathTxtView.text = "" + model.math
            scienceTxtView.text = "" + model.science
            englishTxtView.text = "" + model.english
            kiswahiliTxtView.text = "" + model.kiswahili
            sstCreTxtView.text = "" + model.sst_cre
            classGradeTxtView.text = "Class: " + model.classGrade
          //  val totalMarks = model.math + model.science + model.english + model.kiswahili + model.sst_cre
            totalMarkTxtView.text = "" + model.totalMarks

            editTxtView.setOnClickListener {
                onEditClicked(getItem(position))
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_results, parent, false)
        val binding = ItemResultsBinding.bind(view)
        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: ItemResultsBinding) : RecyclerView.ViewHolder(binding.root)
    private fun computeTotalMarks(studentMarks: StudentMarks):Int {
        studentMarks.totalMarks =
            studentMarks.math.toIntOrZero() + studentMarks.science.toIntOrZero() + studentMarks.english.toIntOrZero() + studentMarks.kiswahili.toIntOrZero() + studentMarks.sst_cre.toIntOrZero()
        return studentMarks.totalMarks
    }
}