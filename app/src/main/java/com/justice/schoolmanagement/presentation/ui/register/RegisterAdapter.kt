package com.justice.schoolmanagement.presentation.ui.register

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemRegisterBinding

class RegisterAdapter(private val requestManager: RequestManager,private val checkBoxClicked:(DocumentSnapshot,Boolean)->Unit) : ListAdapter<DocumentSnapshot, RegisterAdapter.ViewHolder>(DIFF_UTIL) {


    private val TAG = "RegisterAdapter"

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot) = oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(StudentRegistrationData::class.java)
                val new = newItem.toObject(StudentRegistrationData::class.java)
                return old == new
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: ")
        val model = getItem(position).toObject(StudentRegistrationData::class.java)!!
        val studentData = model.studentData

        holder.binding.studentNameTxtView.text = studentData?.fullName
        holder.binding.studentClassTxtView.text = "" + studentData?.classGrade

        if (model.present) {
            holder.binding.root.setBackgroundColor(Color.GREEN)
        } else {
            holder.binding.root.setBackgroundColor(Color.RED)
        }

        holder.binding.checkbox.isChecked = model.present
        requestManager.load(studentData?.photo).thumbnail(requestManager.load(studentData?.thumbnail)).into(holder.binding.imageView)
    }





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_register, parent, false)
        val binding: ItemRegisterBinding = ItemRegisterBinding.bind(view)
        return ViewHolder(binding)
    }


    inner class ViewHolder(val binding: ItemRegisterBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.checkbox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isPresent: Boolean) {
                    val model = getItem(bindingAdapterPosition)
                    writeToDatabase(getItem(bindingAdapterPosition), isPresent)

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