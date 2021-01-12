package com.justice.schoolmanagement.presentation.ui.parent

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemParentsBinding
import com.justice.schoolmanagement.parent.EditParentActivity
import com.justice.schoolmanagement.parent.ParentDetailsActivity
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import es.dmoral.toasty.Toasty

class ParentsActivityRecyclerAdapter
/**
 * Create a new RecyclerView adapter that listens to a Firestore Query.  See [ ] for configuration options.
 *
 * @param options
 */(private val parentsFragment: ParentsFragment, options: FirestoreRecyclerOptions<ParentData?>) : FirestoreRecyclerAdapter<ParentData, ParentsActivityRecyclerAdapter.ViewHolder>(options) {
    private val context: Context? = null
    private val parentData: ParentData? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ParentData) {



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
            val intent = Intent(context, EditParentActivity::class.java)
            ApplicationClass.documentSnapshot = snapshots.getSnapshot(position)
            context!!.startActivity(intent)
        }
        holder.itemView.setOnClickListener(View.OnClickListener {
            if (position == RecyclerView.NO_POSITION) {
                return@OnClickListener
            }
            val intent = Intent(context, ParentDetailsActivity::class.java)
            ApplicationClass.documentSnapshot = snapshots.getSnapshot(position)
            context!!.startActivity(intent)
        })
    }

    fun deleteFromDatabase(position: Int) {
        MaterialAlertDialogBuilder(context!!).setBackground(context.getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> notifyItemChanged(position) }.setPositiveButton("yes") { dialog, which -> deleteParent(position) }.show()
    }

    private fun deleteParent(position: Int) {
        parentsFragment.showProgress(true)
        FirebaseStorage.getInstance().getReferenceFromUrl(snapshots.getSnapshot(position).toObject(ParentData::class.java)!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(context!!, "Photo Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(context!!, "Error: $error", Toast.LENGTH_SHORT).show()
            }
            parentsFragment.showProgress(false)
        }
        snapshots.getSnapshot(position).reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(context!!, "Deletion Success", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(context!!, "Error: $error", Toast.LENGTH_SHORT).show()
            }
            parentsFragment.showProgress(false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_parents, parent, false)
       val binding:ItemParentsBinding= ItemParentsBinding.bind(view)

        return ViewHolder(binding)
    }

    fun getSwipedItem(position: Int): DocumentReference {
        return snapshots.getSnapshot(position).reference
    }

    inner class ViewHolder(val binding: ItemParentsBinding) : RecyclerView.ViewHolder(binding.root) {

    }

}