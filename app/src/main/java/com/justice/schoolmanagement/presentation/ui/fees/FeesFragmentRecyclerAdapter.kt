package com.justice.schoolmanagement.presentation.ui.fees

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemFeesBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat

class FeesFragmentRecyclerAdapter(private val feesFragment: FeesFragment, options: FirestoreRecyclerOptions<StudentFees?>) : FirestoreRecyclerAdapter<StudentFees, FeesFragmentRecyclerAdapter.ViewHolder>(options) {
    lateinit var currentSnapShot: DocumentSnapshot
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: StudentFees) {
        holder.binding.dateEdtTxt.setText(SimpleDateFormat.getDateTimeInstance().format(model.date))
        holder.binding.payedEdtTxt.setText(model.payedAmount.toString())
        setOnClickListeners(holder, position)

    }

    private fun setOnClickListeners(holder: ViewHolder, position: Int) {
        holder.binding.deleteTxtView.setOnClickListener { deleteStudentFromDatabase(position) }
        holder.binding.editTxtView.setOnClickListener {
            ApplicationClass.documentSnapshot = snapshots.getSnapshot(position)
            feesFragment.navController.navigate(R.id.action_feesFragment_to_feesEditFragment)
        }

    }

    fun deleteStudentFromDatabase(position: Int) {
        MaterialAlertDialogBuilder(feesFragment.requireContext()).setBackground(feesFragment.requireActivity().getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no") { dialog, which -> notifyItemChanged(position) }.setPositiveButton("yes") { dialog, which -> deleteStudentFees(position) }.show()
    }

    private fun deleteStudentFees(position: Int) {
        currentSnapShot = snapshots.getSnapshot(position)
        feesFragment.showProgress(true)
        deleteStudentMetaData();


    }


    private fun deleteStudentMetaData() {
        currentSnapShot.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(feesFragment.requireContext(), "Student Fees Deleted ", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(feesFragment.requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            feesFragment.showProgress(false)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fees, parent, false)
        val binding: ItemFeesBinding = ItemFeesBinding.bind(view)
        return ViewHolder(binding)
    }

    fun getSwipedItem(position: Int): DocumentReference {
        return snapshots.getSnapshot(position).reference
    }

    inner class ViewHolder(val binding: ItemFeesBinding) : RecyclerView.ViewHolder(binding.root) {

    }


}