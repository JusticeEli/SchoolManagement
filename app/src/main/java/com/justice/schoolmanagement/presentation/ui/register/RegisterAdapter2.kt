package com.justice.schoolmanagement.presentation.ui.register

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemRegisterBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import es.dmoral.toasty.Toasty

class RegisterAdapter2(private val studentsFragment: Fragment, options: FirestoreRecyclerOptions<StudentRegistrationData>) : FirestoreRecyclerAdapter<StudentRegistrationData, RegisterAdapter2.ViewHolder>(options) {
    companion object {
        private const val TAG = "RegisterAdapter"
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: StudentRegistrationData) {

        Log.d(TAG, "onBindViewHolder: ")

        val studentData = model.studentData

        holder.binding.studentNameTxtView.text = studentData?.fullName
        holder.binding.studentClassTxtView.text = "" + studentData?.classGrade

        if (model.present) {
            holder.binding.root.setBackgroundColor(Color.GREEN)
        } else {
            holder.binding.root.setBackgroundColor(Color.RED)

        }
        holder.binding.checkbox.isChecked = model.present
        val requestOptions = RequestOptions()
        requestOptions.centerCrop()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(studentsFragment.requireContext()).applyDefaultRequestOptions(requestOptions).load(studentData?.photo).thumbnail(Glide.with(studentsFragment.requireContext()).load(studentData?.thumbnail)).into(holder.binding.imageView)
        setOnClickListeners(holder, position)
    }

    private fun setOnClickListeners(holder: ViewHolder, position: Int) {
        //  holder.binding.deleteTxtView.setOnClickListener { deleteStudentFromDatabase(position) }
        /*   holder.binding.editTxtView.setOnClickListener {
             //   ApplicationClass.documentSnapshot = snapshots.getSnapshot(position).toObject(StudentRegistrationData::class.java)?.documentSnapshot
           //    studentsFragment.navController.navigate(StudentsFragmentDirections.actionStudentsFragmentToEditStudentFragment())
           }
           holder.itemView.setOnClickListener {
          //     ApplicationClass.documentSnapshot = snapshots.getSnapshot(position).toObject(StudentRegistrationData::class.java)?.documentSnapshot
      //        studentsFragment.navController.navigate(StudentsFragmentDirections.actionStudentsFragmentToStudentDetailsFragment())
           }*/
    }

    fun deleteStudentFromDatabase(position: Int) {
        MaterialAlertDialogBuilder(studentsFragment.requireContext()).setBackground(studentsFragment.requireActivity().getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> notifyItemChanged(position) }.setPositiveButton("yes") { dialog, which -> deleteStudent(position) }.show()
    }

    private fun deleteStudent(position: Int) {
        (studentsFragment as AllFragment).showProgress(true)
        FirebaseStorage.getInstance().getReferenceFromUrl(snapshots.getSnapshot(position).toObject(StudentData::class.java)!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(studentsFragment.requireContext(), "Photo Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(studentsFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            studentsFragment.showProgress(false)
        }
        snapshots.getSnapshot(position).reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(studentsFragment.requireContext(), "StudentData Deleted ", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(studentsFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            studentsFragment.showProgress(false)
        }
        FirebaseFirestore.getInstance().collection("StudentsMarks").document(snapshots.getSnapshot(position).id).delete().addOnCompleteListener { task ->
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_register, parent, false)
        val binding: ItemRegisterBinding = ItemRegisterBinding.bind(view)
        return ViewHolder(binding)
    }

    fun getSwipedItem(position: Int): DocumentReference {
        return snapshots.getSnapshot(position).reference
    }

    inner class ViewHolder(val binding: ItemRegisterBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.checkbox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isPresent: Boolean) {
                    val model = snapshots.get(bindingAdapterPosition)
                    writeToDatabase(snapshots.getSnapshot(bindingAdapterPosition), isPresent)

                }
            })

        }
    }

    private fun writeToDatabase(snapshot: DocumentSnapshot, isPresent: Boolean) {
        val map = mapOf<String, Boolean>("present" to isPresent)
        snapshot.reference.set(map, SetOptions.merge()).addOnSuccessListener {
            Log.d(TAG, "writeToDatabase: success student status changed")
        }

    }


}