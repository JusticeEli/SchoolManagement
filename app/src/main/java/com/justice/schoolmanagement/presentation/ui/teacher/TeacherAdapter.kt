package com.justice.schoolmanagement.presentation.ui.teacher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemTeachersBinding
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData


class TeacherAdapter(private val requestManager: RequestManager, private val onEditClicked: (DocumentSnapshot) -> Unit, private val onTeacherClicked: (DocumentSnapshot) -> Unit, private val onTeacherDelete: (DocumentSnapshot) -> Unit) : ListAdapter<DocumentSnapshot, TeacherAdapter.ViewHolder>(DIFF_UTIL) {


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
            requestManager.load(model.photo).thumbnail(requestManager.load(model.thumbnail)).into(imageView)
            setOnClickListeners(holder, position)
            setOnClickListeners(holder, position)
        }

    }


    private fun setOnClickListeners(holder: ViewHolder, position: Int) {

        var currentSnapshot: DocumentSnapshot
        try {
            currentSnapshot = getItem(position)
        } catch (e: IndexOutOfBoundsException) {
            currentSnapshot = getItem(position - 1)
            e.printStackTrace()
        }


        holder.binding.deleteTxtView.setOnClickListener {
            onTeacherDelete(currentSnapshot)
        }
        holder.binding.editTxtView.setOnClickListener {
            onEditClicked(currentSnapshot)
        }
        holder.itemView.setOnClickListener {
            onTeacherClicked(currentSnapshot)
        }
    }

    inner class ViewHolder(val binding: ItemTeachersBinding) : RecyclerView.ViewHolder(binding.root)

}