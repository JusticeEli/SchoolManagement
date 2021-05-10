package com.justice.schoolmanagement.presentation.ui.parent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemParentsBinding
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData

class ParentsAdapter(private val requestManager: RequestManager, private val onEditClicked: (DocumentSnapshot) -> Unit, private val onParentClicked: (DocumentSnapshot) -> Unit, private val onParentDelete: (DocumentSnapshot) -> Unit) : ListAdapter<DocumentSnapshot, ParentsAdapter.ViewHolder>(DIFF_UTIL) {

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot) = oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(ParentData::class.java)
                val new = newItem.toObject(ParentData::class.java)
                return old == new
            }
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
            onParentDelete(currentSnapshot)
        }
        holder.binding.editTxtView.setOnClickListener {
            onEditClicked(currentSnapshot)
        }
        holder.itemView.setOnClickListener(View.OnClickListener {
            if (position == RecyclerView.NO_POSITION) {
                return@OnClickListener
            }
            onParentClicked(currentSnapshot)
        })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_parents, parent, false)
        val binding: ItemParentsBinding = ItemParentsBinding.bind(view)

        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position).toObject(ParentData::class.java)!!
        holder.binding.apply {
            parentNameTxtView.text = model.fullName
            parentContactTxtView.text = model.contact
        }
        val requestOptions = RequestOptions()
        requestOptions.centerCrop()
        requestOptions.placeholder(R.mipmap.place_holder)
        requestManager.load(model.photo).thumbnail(requestManager.load(model.thumbnail)).into(holder.binding.imageView)
        setOnClickListeners(holder, position)
    }

    inner class ViewHolder(val binding: ItemParentsBinding) : RecyclerView.ViewHolder(binding.root)
}