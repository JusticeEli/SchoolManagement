package com.justice.schoolmanagement.presentation.ui.parent

import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemParentsBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import es.dmoral.toasty.Toasty

class ParentFilterAdapter(val parentsFragment: ParentsFragment) : ListAdapter<DocumentSnapshot, ParentFilterAdapter.ViewHolder>(DIFF_UTIL), Filterable {

    val context = parentsFragment.requireContext()

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(ParentData::class.java)
                val new = newItem.toObject(ParentData::class.java)
                return old!!.equals(new)
            }

        }
    }

    inner class ViewHolder(val binding: ItemParentsBinding) : RecyclerView.ViewHolder(binding.root) {

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
        Glide.with(context!!).applyDefaultRequestOptions(requestOptions).load(model.photo).thumbnail(Glide.with(context).load(model.thumbnail)).into(holder.binding.imageView)
        setOnClickListeners(holder, position)
    }


    private fun setOnClickListeners(holder: ViewHolder, position: Int) {
        holder.binding.deleteTxtView.setOnClickListener { deleteFromDatabase(position) }
        holder.binding.editTxtView.setOnClickListener {
            ApplicationClass.documentSnapshot = getItem(position)
            parentsFragment.navController.navigate(R.id.action_parentsFragment_to_editParentFragment)
        }
        holder.itemView.setOnClickListener(View.OnClickListener {
            if (position == RecyclerView.NO_POSITION) {
                return@OnClickListener
            }
            ApplicationClass.documentSnapshot = getItem(position)

            parentsFragment.navController.navigate(ParentsFragmentDirections.actionParentsFragmentToParentDetailsFragment(ApplicationClass.documentSnapshot!!.getString("email")!!))
        })
    }

    fun deleteFromDatabase(position: Int) {
        MaterialAlertDialogBuilder(context!!).setBackground(context.getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> notifyItemChanged(position) }.setPositiveButton("yes") { dialog, which -> deleteParent(position) }.show()
    }

    private fun deleteParent(position: Int) {
        parentsFragment.showProgress(true)
        FirebaseStorage.getInstance().getReferenceFromUrl(getItem(position).toObject(ParentData::class.java)!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(context!!, "Photo Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(context!!, "Error: $error", Toast.LENGTH_SHORT).show()
            }
            parentsFragment.showProgress(false)
        }
        getItem(position).reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(context!!, "Deletion Success", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(context!!, "Error: $error", Toast.LENGTH_SHORT).show()
            }
            parentsFragment.showProgress(false)
        }

    }


    override fun getFilter(): Filter {
        return filter
    }

    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            Log.d("ParentFilterAdapter", "performFiltering: filtering")
            val filterResults: FilterResults = Filter.FilterResults()
            val filterList = mutableListOf<DocumentSnapshot>()

            parentsFragment.originalList.forEach { documentSnapshot ->
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