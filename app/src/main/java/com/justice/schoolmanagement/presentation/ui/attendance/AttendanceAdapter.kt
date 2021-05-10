package com.justice.schoolmanagement.presentation.ui.attendance

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemAttendanceBinding
import com.justice.schoolmanagement.presentation.ui.attendance.model.CheckInOut
import com.justice.schoolmanagement.utils.formatDate

class AttendanceAdapter(private val requestManager: RequestManager) : ListAdapter<DocumentSnapshot, AttendanceAdapter.ViewHolder>(DIFF_UTIL) {


    private val TAG = "AttendanceAdapter"

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot) = oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(CheckInOut::class.java)
                val new = newItem.toObject(CheckInOut::class.java)
                return old == new
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val model = getItem(position).toObject(CheckInOut::class.java)!!
            Log.d(TAG, "onBindViewHolder: model:$model")
            nameTxtView.text = model.fullName
            model.checkInTime?.let {
                checkInTxtView.text = it.formatDate

            }
            if (model.checkOut) {
                model.checkOutTime?.let {
                    checkOutTxtView.text =it.formatDate

                }
            }
          requestManager.load(model.image).into(imageView)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)
        val binding: ItemAttendanceBinding = ItemAttendanceBinding.bind(view)

        return ViewHolder(binding)
    }



    inner class ViewHolder(val binding: ItemAttendanceBinding) : RecyclerView.ViewHolder(binding.root)


}