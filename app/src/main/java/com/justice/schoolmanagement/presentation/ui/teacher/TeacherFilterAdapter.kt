package com.justice.schoolmanagement.presentation.ui.teacher

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemTeachersBinding
import com.justice.schoolmanagement.presentation.SchoolApplication
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import es.dmoral.toasty.Toasty


class TeacherFilterAdapter(val teachersFragment: TeachersFragment) : ListAdapter<DocumentSnapshot, TeacherFilterAdapter.ViewHolder>(DIFF_UTIL), Filterable {
    private var teacherData: TeacherData? = null
private lateinit var currentSnapshot: DocumentSnapshot
    val context = teachersFragment.requireContext()

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(TeacherData::class.java)
                val new = newItem.toObject(TeacherData::class.java)
                return old!!.equals(new)
            }

        }
    }

    inner class ViewHolder(val binding: ItemTeachersBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_teachers, parent, false)
        val binding: ItemTeachersBinding = ItemTeachersBinding.bind(view)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position).toObject(TeacherData::class.java)!!




        holder.binding.apply {

            teacherNameTxtView.text = model.fullName
            teacherSubjectTxtView.text = model.subject
            val requestOptions = RequestOptions()
            requestOptions.centerCrop()
            requestOptions.placeholder(R.mipmap.place_holder)
            Glide.with(teachersFragment.requireActivity()).applyDefaultRequestOptions(requestOptions).load(model.photo).thumbnail(Glide.with(teachersFragment.requireActivity()).load(model.thumbnail)).into(imageView)
            setOnClickListeners(holder, position)
            setOnClickListeners(holder, position)
        }

    }


    private fun setOnClickListeners(holder: ViewHolder, position: Int) {
        holder.binding.deleteTxtView.setOnClickListener { deleteTeacherDataFromDatabase(position) }
        holder.binding.editTxtView.setOnClickListener {
            SchoolApplication.documentSnapshot = getItem(position)
            teachersFragment.navController.navigate(R.id.action_teachersFragment_to_editTeacherFragment)
        }
        holder.itemView.setOnClickListener {
            SchoolApplication.documentSnapshot = getItem(position)
            teachersFragment.navController.navigate(R.id.action_teachersFragment_to_teacherDetailsFragment)
        }
    }

    fun deleteTeacherDataFromDatabase(position: Int) {
        MaterialAlertDialogBuilder(teachersFragment.requireActivity()).setBackground(ContextCompat.getDrawable(teachersFragment.requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> notifyItemChanged(position) }.setPositiveButton("yes") { dialog, which -> deleteTeacher(position) }.show()
    }

    fun getSwipedItem(position: Int): DocumentReference {
        return getItem(position).reference
    }

    private fun deleteTeacher(position: Int) {
       currentSnapshot = getItem(position)
        teacherData = getItem(position).toObject(TeacherData::class.java)
        teachersFragment.showProgress(true)
        FirebaseStorage.getInstance().getReferenceFromUrl(currentSnapshot.toObject(TeacherData::class.java)!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(teachersFragment.requireActivity(), "Photo Deleted", Toast.LENGTH_SHORT).show()
                deleteTeacherMetaData()
            } else {
                val error = task.exception!!.message
                Toasty.error(teachersFragment.requireActivity(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
         }

    }

    private fun deleteTeacherMetaData() {
       currentSnapshot.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.error(teachersFragment.requireActivity(), "Teacher Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(teachersFragment.requireActivity(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            teachersFragment.showProgress(false)
        }   }

    override fun getFilter(): Filter {
        return filter
    }

    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            Log.d("ParentFilterAdapter", "performFiltering: filtering")
            val filterResults: FilterResults = Filter.FilterResults()
            val filterList = mutableListOf<DocumentSnapshot>()

            teachersFragment.originalList.forEach { documentSnapshot ->
                val parentData = documentSnapshot.toObject(ParentData::class.java)!!
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

            Log.d("ParentFilterAdapter", "publishResults: results publshed")
        }
    }


}