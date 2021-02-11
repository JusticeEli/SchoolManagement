package com.justice.schoolmanagement.presentation.ui.attendance.model

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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemAttendanceBinding
import com.justice.schoolmanagement.presentation.ui.attendance.AttendanceFragment
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat

class AttendanceFragmentAdapter(private val attendanceFragment: AttendanceFragment, options: FirestoreRecyclerOptions<CheckInOut?>) : FirestoreRecyclerAdapter<CheckInOut, AttendanceFragmentAdapter.ViewHolder>(options) {

    private lateinit var currentSnapshot: DocumentSnapshot
    private var teacherData: TeacherData? = null
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: CheckInOut) {
        holder.binding.apply {

            nameTxtView.text = model.fullName
            checkInTxtView.text = SimpleDateFormat.getDateTimeInstance().format(model.checkInTime)

            if (model.checkOut){
                checkOutTxtView.text = SimpleDateFormat.getDateTimeInstance().format(model.checkOutTime)

            }
           val requestOptions = RequestOptions()
            requestOptions.centerCrop()
            requestOptions.placeholder(R.mipmap.place_holder)
            Glide.with(attendanceFragment.requireActivity()).applyDefaultRequestOptions(requestOptions).load(model.image).into(imageView)
           }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)
        val binding: ItemAttendanceBinding = ItemAttendanceBinding.bind(view)

        return ViewHolder(binding)
    }


    fun deleteTeacherDataFromDatabase(position: Int) {
        MaterialAlertDialogBuilder(attendanceFragment.requireActivity()).setBackground(ContextCompat.getDrawable(attendanceFragment.requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> notifyItemChanged(position) }.setPositiveButton("yes") { dialog, which -> deleteTeacherMetaData(position) }.show()
    }

    fun getSwipedItem(position: Int): DocumentReference {
        return snapshots.getSnapshot(position).reference
    }



    private fun deleteTeacherMetaData(position:Int) {
        snapshots.getSnapshot(position).reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(attendanceFragment.requireActivity(), "Teacher Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(attendanceFragment.requireActivity(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            attendanceFragment.showProgress(false)
        } }

    inner class ViewHolder(val binding: ItemAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}