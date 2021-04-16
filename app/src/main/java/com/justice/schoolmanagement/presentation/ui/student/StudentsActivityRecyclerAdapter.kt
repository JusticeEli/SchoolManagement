package com.justice.schoolmanagement.presentation.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemStudentsBinding
import com.justice.schoolmanagement.presentation.SchoolApplication
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty

class StudentsActivityRecyclerAdapter(private val studentsFragment: StudentsFragment, options: FirestoreRecyclerOptions<StudentData?>) : FirestoreRecyclerAdapter<StudentData, StudentsActivityRecyclerAdapter.ViewHolder>(options) {
    lateinit var currentSnapShot: DocumentSnapshot
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: StudentData) {
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
            SchoolApplication.documentSnapshot = snapshots.getSnapshot(position)
            studentsFragment.navController.navigate(StudentsFragmentDirections.actionStudentsFragmentToEditStudentFragment())
        }
        holder.itemView.setOnClickListener {
            SchoolApplication.documentSnapshot = snapshots.getSnapshot(position)
            studentsFragment.navController.navigate(StudentsFragmentDirections.actionStudentsFragmentToStudentDetailsFragment())
        }
    }

    fun deleteStudentFromDatabase(position: Int) {
        MaterialAlertDialogBuilder(studentsFragment.requireContext()).setBackground(studentsFragment.requireActivity().getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> notifyItemChanged(position) }.setPositiveButton("yes") { dialog, which -> deleteStudentPhoto(position) }.show()
    }

    private fun deleteStudentPhoto(position: Int) {
        currentSnapShot = snapshots.getSnapshot(position)
        studentsFragment.showProgress(true)
        FirebaseStorage.getInstance().getReferenceFromUrl(currentSnapShot.toObject(StudentData::class.java)!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(studentsFragment.requireContext(), "Photo Deleted", Toast.LENGTH_SHORT).show()
                deleteStudentMetaData();

            } else {
                val error = task.exception!!.message
                Toasty.error(studentsFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }

        }


    }

    private fun deleteStudentMetaData() {
        currentSnapShot.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                deleteStudentMarks()
                Toasty.success(studentsFragment.requireContext(), "StudentData Deleted ", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(studentsFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteStudentMarks() {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_MARKS).document(currentSnapShot.id).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(studentsFragment.requireContext(), "StudentsMarks Deleted ", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(studentsFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            studentsFragment.showProgress(false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_students, parent, false)
        val binding: ItemStudentsBinding = ItemStudentsBinding.bind(view)
        return ViewHolder(binding)
    }

    fun getSwipedItem(position: Int): DocumentReference {
        return snapshots.getSnapshot(position).reference
    }

    inner class ViewHolder(val binding: ItemStudentsBinding) : RecyclerView.ViewHolder(binding.root) {

    }


}