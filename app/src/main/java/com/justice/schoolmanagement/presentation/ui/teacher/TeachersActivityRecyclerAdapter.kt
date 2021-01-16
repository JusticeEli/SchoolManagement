package com.justice.schoolmanagement.presentation.ui.teacher

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemTeachersBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import es.dmoral.toasty.Toasty

class TeachersActivityRecyclerAdapter(private val teachersFragment: TeachersFragment, options: FirestoreRecyclerOptions<TeacherData?>) : FirestoreRecyclerAdapter<TeacherData, TeachersActivityRecyclerAdapter.ViewHolder>(options) {
    private var teacherData: TeacherData? = null
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: TeacherData) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_teachers, parent, false)
        val binding: ItemTeachersBinding = ItemTeachersBinding.bind(view)

        return ViewHolder(binding)
    }

    private fun setOnClickListeners(holder: ViewHolder, position: Int) {
        holder.binding.deleteTxtView.setOnClickListener { deleteTeacherDataFromDatabase(position) }
        holder.binding.editTxtView.setOnClickListener {
            ApplicationClass.documentSnapshot = snapshots.getSnapshot(position)
            teachersFragment.navController.navigate(R.id.action_teachersFragment_to_editTeacherFragment)
        }
        holder.itemView.setOnClickListener {
            ApplicationClass.documentSnapshot = snapshots.getSnapshot(position)
            teachersFragment.navController.navigate(R.id.action_teachersFragment_to_teacherDetailsFragment)
        }
    }

    fun deleteTeacherDataFromDatabase(position: Int) {
        MaterialAlertDialogBuilder(teachersFragment.requireActivity()).setBackground(ContextCompat.getDrawable(teachersFragment.requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> notifyItemChanged(position) }.setPositiveButton("yes") { dialog, which -> deleteTeacher(position) }.show()
    }

    fun getSwipedItem(position: Int): DocumentReference {
        return snapshots.getSnapshot(position).reference
    }

    private fun deleteTeacher(position: Int) {
        val documentSnapshot = snapshots.getSnapshot(position)
        teacherData = snapshots.getSnapshot(position).toObject(TeacherData::class.java)
        teachersFragment.showProgress(true)
        FirebaseStorage.getInstance().getReference("teachers_images").child(documentSnapshot.id + ".jpg").delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(teachersFragment.requireActivity(), "Photo Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(teachersFragment.requireActivity(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            teachersFragment.showProgress(false)
        }
        snapshots.getSnapshot(position).reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.error(teachersFragment.requireActivity(), "Teacher Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(teachersFragment.requireActivity(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            teachersFragment.showProgress(false)
        }
    }

    inner class ViewHolder(val binding: ItemTeachersBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}