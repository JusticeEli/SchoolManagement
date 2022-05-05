package com.justice.schoolmanagement.presentation.ui.fees

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemFeesBinding
import java.text.SimpleDateFormat

class FeesAdapter(private val onDelete: (DocumentSnapshot) -> Unit, private val onEdit: (DocumentSnapshot) -> Unit) : ListAdapter<DocumentSnapshot, FeesAdapter.ViewHolder>(DIFF_UTIL) {
    private  val TAG = "FeesAdapter"
    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot) = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(StudentFees::class.java)
                val new = newItem.toObject(StudentFees::class.java)
                return old == new
            }
        }
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position).toObject(StudentFees::class.java)!!

        try{
            holder.binding.dateEdtTxt.setText(SimpleDateFormat.getDateTimeInstance().format(model.date))

        }catch (e:Exception){
            Log.e(TAG, "onBindViewHolder: date object is null",e )
        }
        holder.binding.payedEdtTxt.setText(model.payedAmount.toString())
        setOnClickListeners(holder, position)
    }

    private fun setOnClickListeners(holder: ViewHolder, position: Int) {
        val currentSnapshot = getItem(position)
        holder.binding.deleteTxtView.setOnClickListener { onDelete(currentSnapshot) }
        holder.binding.editTxtView.setOnClickListener {
            onEdit(currentSnapshot)
        }

    }






    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fees, parent, false)
        val binding: ItemFeesBinding = ItemFeesBinding.bind(view)
        return ViewHolder(binding)
    }



    inner class ViewHolder(val binding: ItemFeesBinding) : RecyclerView.ViewHolder(binding.root)

}