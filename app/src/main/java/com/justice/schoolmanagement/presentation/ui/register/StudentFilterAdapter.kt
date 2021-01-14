package com.justice.schoolmanagement.presentation.ui.register

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemStudentsBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.student.StudentsFragment
import com.justice.schoolmanagement.presentation.ui.student.StudentsFragmentDirections
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import es.dmoral.toasty.Toasty

class StudentFilterAdapter(val studentsFragment: StudentsFragment) : ListAdapter<DocumentSnapshot, StudentFilterAdapter.ViewHolder>(DIFF_UTIL), Filterable {
private  val TAG="ParentFilterAdapter"
    val context = studentsFragment.requireContext()

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

    inner class ViewHolder(val binding: ItemStudentsBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_students, parent, false)
        val binding:ItemStudentsBinding= ItemStudentsBinding.bind(view)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position).toObject(StudentData::class.java)!!
        holder.binding.studentNameTxtView.text = model.fullName
        holder.binding.studentClassTxtView.text = "" + model.classGrade
        val requestOptions = RequestOptions()
        requestOptions.centerCrop()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(studentsFragment.requireContext()).applyDefaultRequestOptions(requestOptions).load(model.photo).thumbnail(Glide.with(studentsFragment.requireContext()).load(model.thumbnail)).into(holder.binding.imageView)
        setOnClickListeners(holder, position)

    }


    private fun setOnClickListeners(holder: ViewHolder, position: Int) {
        holder.binding.deleteTxtView.setOnClickListener { deleteStudentFromDatabase(position) }
        holder.binding.editTxtView.setOnClickListener {
            ApplicationClass.documentSnapshot = getItem(position)
            studentsFragment.navController.navigate(StudentsFragmentDirections.actionStudentsFragmentToEditStudentFragment())
        }
        holder.itemView.setOnClickListener {
            ApplicationClass.documentSnapshot = getItem(position)
            studentsFragment.navController.navigate(StudentsFragmentDirections.actionStudentsFragmentToStudentDetailsFragment())
        }
    }

    fun deleteStudentFromDatabase(position: Int) {
        MaterialAlertDialogBuilder(studentsFragment.requireContext()).setBackground(studentsFragment.requireActivity().getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> notifyItemChanged(position) }.setPositiveButton("yes") { dialog, which -> deleteStudent(position) }.show()
    }

    private fun deleteStudent(position: Int) {
        studentsFragment.showProgress(true)
        FirebaseStorage.getInstance().getReferenceFromUrl(getItem(position).toObject(StudentData::class.java)!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(studentsFragment.requireContext(), "Photo Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(studentsFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            studentsFragment.showProgress(false)
        }
        getItem(position).reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(studentsFragment.requireContext(), "StudentData Deleted ", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(studentsFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            studentsFragment.showProgress(false)
        }
        FirebaseFirestore.getInstance().collection("StudentsMarks").document(getItem(position).id).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(studentsFragment.requireContext(), "StudentsMarks Deleted ", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(studentsFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            studentsFragment.showProgress(false)
        }
    }



    override fun getFilter(): Filter {
        return filter
    }

    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            Log.d(TAG, "performFiltering: filtering")
            val filterResults: FilterResults = Filter.FilterResults()
            val filterList = mutableListOf<DocumentSnapshot>()

            studentsFragment.originalList.forEach { documentSnapshot ->
                val parentData = documentSnapshot.toObject(StudentData::class.java)!!
                /* if (parentData.fullName.contains(constraint!!)||parentData.email.contains(constraint!!)||parentData.jobType.contains(constraint!!)){

                     filterList.add(documentSnapshot)
                 }*/
                if (parentData.fullName.toLowerCase().contains(constraint!!.toString().toLowerCase())) {
                    filterList.add(documentSnapshot)
                }
            }

            filterResults.values = filterList
            return filterResults

        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val filteredList = results?.values as List<DocumentSnapshot>
            submitList(filteredList)

            Log.d(TAG, "publishResults: results publshed")
        }
    }


}