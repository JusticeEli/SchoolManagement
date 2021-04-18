package com.justice.schoolmanagement.presentation.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemStudentsBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData

class StudentAdapter(private val requestManager: RequestManager, private val onEditClicked: (DocumentSnapshot) -> Unit, private val onStudentClicked: (DocumentSnapshot) -> Unit, private val onStudentDelete: (DocumentSnapshot) -> Unit) : ListAdapter<DocumentSnapshot, StudentAdapter.ViewHolder>(DIFF_UTIL) {
    private val TAG = "ParentFilterAdapter"

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(StudentData::class.java)
                val new = newItem.toObject(StudentData::class.java)
                return old!!.equals(new)
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_students, parent, false)
        val binding: ItemStudentsBinding = ItemStudentsBinding.bind(view)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: StudentAdapter.ViewHolder, position: Int) {
        val model = getItem(position).toObject(StudentData::class.java)!!
        holder.binding.studentNameTxtView.text = model.fullName
        holder.binding.studentClassTxtView.text = "${model.classGrade}"
        requestManager.load(model.photo).thumbnail(requestManager.load(model.thumbnail)).into(holder.binding.imageView)
        setOnClickListeners(holder, position)
    }

    private fun setOnClickListeners(holder: ViewHolder, position: Int) {

        var currentSnapshot: DocumentSnapshot
        try {
            currentSnapshot = getItem(position)
        } catch (e: IndexOutOfBoundsException) {
            currentSnapshot = getItem(position - 1)

        }


        holder.binding.deleteTxtView.setOnClickListener {
            onStudentDelete(currentSnapshot)
         }
        holder.binding.editTxtView.setOnClickListener {
            onEditClicked(currentSnapshot)
          }
        holder.itemView.setOnClickListener {
            onStudentClicked(currentSnapshot)
           }
    }

    inner class ViewHolder(val binding: ItemStudentsBinding) : RecyclerView.ViewHolder(binding.root)

}