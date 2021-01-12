package com.justice.schoolmanagement.presentation.ui.class_

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemStudentsBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import es.dmoral.toasty.Toasty

class ChoosenClassActivityStudentsRecyclerAdapter(private val choosenClassFragment: ChoosenClassFragment, options: FirestoreRecyclerOptions<StudentData?>) : FirestoreRecyclerAdapter<StudentData, ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder>(options) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: StudentData) {
        holder.binding.studentNameTxtView.text = model.fullName
        holder.binding.studentClassTxtView.text = "" + model.classGrade
        val requestOptions = RequestOptions()
        requestOptions.centerCrop()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(choosenClassFragment.requireContext()).applyDefaultRequestOptions(requestOptions).load(model.photo).thumbnail(Glide.with(choosenClassFragment.requireContext()).load(model.thumbnail)).into(holder.binding.imageView)
        setOnClickListeners(holder, position)
    }

    private fun setOnClickListeners(holder: ViewHolder, position: Int) {
        holder.binding.deleteTxtView.setOnClickListener { deleteStudentFromDatabase(position) }
        holder.binding.editTxtView.setOnClickListener {
            ApplicationClass.documentSnapshot = snapshots.getSnapshot(position)

            choosenClassFragment.navController.navigate(R.id.action_choosenClassFragment_to_editStudentActivity)
        }
        holder.itemView.setOnClickListener {
            ApplicationClass.documentSnapshot = snapshots.getSnapshot(position)
            choosenClassFragment.navController.navigate(R.id.action_choosenClassFragment_to_studentDetailsFragment)
        }
    }

    private fun deleteStudentFromDatabase(position: Int) {
        val builder = AlertDialog.Builder(choosenClassFragment.requireContext()).setTitle("Delete").setMessage("Are You Sure you Want To delete!!").setNegativeButton("No") { dialog, which -> }.setPositiveButton("Yes") { dialog, which -> deleteStudent(position) }
        builder.show()
    }

    private fun deleteStudent(position: Int) {
        choosenClassFragment.showProgress(true)
        FirebaseStorage.getInstance().getReferenceFromUrl(snapshots.getSnapshot(position).toObject(StudentData::class.java)!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(choosenClassFragment.requireContext(), "Photo Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(choosenClassFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            choosenClassFragment.showProgress(false)
        }
        snapshots.getSnapshot(position).reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(choosenClassFragment.requireContext(), "StudentData Deleted ", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(choosenClassFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            choosenClassFragment.showProgress(false)
        }
        FirebaseFirestore.getInstance().collection("StudentsMarks").document(snapshots.getSnapshot(position).id).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(choosenClassFragment.requireContext(), "StudentsMarks Deleted ", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(choosenClassFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            choosenClassFragment.showProgress(false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_students, parent, false)
        val binding: ItemStudentsBinding = ItemStudentsBinding.bind(view)

        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: ItemStudentsBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}
