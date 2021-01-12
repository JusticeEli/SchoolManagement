package com.justice.schoolmanagement.presentation.ui.parent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentParentDetailsBinding
import com.justice.schoolmanagement.parent.EditParentActivity
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import es.dmoral.toasty.Toasty

class ParentDetailsFragment : Fragment(R.layout.fragment_parent_details) {

    private var parentData: ParentData? = null
    private var email: String? = null
    lateinit var binding: FragmentParentDetailsBinding
    val navArgs: ParentDetailsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentParentDetailsBinding.bind(view)

        email = navArgs.email

        parentData = ApplicationClass.documentSnapshot!!.toObject(ParentData::class.java)
        parentData!!.id = ApplicationClass.documentSnapshot!!.id
        setDefaultValues()
        setOnClickListeners()
        setImageViewClickListeners()

    }

    private fun setImageViewClickListeners() {
        binding.callImageView.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + parentData!!.contact)
            startActivity(intent)
        })
        binding.emailImageView.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/html"
            val email = arrayOf(parentData!!.email)
            intent.putExtra(Intent.EXTRA_EMAIL, email)
            startActivity(Intent.createChooser(intent, "Choose app to use for sending Email"))
        })
    }

    private fun setOnClickListeners() {
        binding.deleteTxtView.setOnClickListener(View.OnClickListener { deleteFromDatabase() })
        binding.editTxtView.setOnClickListener(View.OnClickListener {
            val intent = Intent(requireContext(), EditParentActivity::class.java)
            startActivity(intent)
        })
    }

    private fun deleteFromDatabase() {
        MaterialAlertDialogBuilder(requireContext()).setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", null).setPositiveButton("yes") { dialog, which -> deleteParent() }.show()
    }

    private fun deleteParent() {
        /**
         * ACTIVITY EXITING BEFORE DELETION OF PHOTO IS COMPLETE MAY CAUSE CRASH OF THE PROGRAM//////////
         *
         */
        showProgress(true)
        FirebaseStorage.getInstance().getReferenceFromUrl(parentData!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Photo Deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
        ApplicationClass.documentSnapshot!!.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), parentData!!.firstName + " Removed Successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                val error = task.exception!!.message
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }

    private fun setDefaultValues() {
        binding.apply {
            parentNameTxtView.setText(parentData!!.firstName + " " + parentData!!.lastName)
            contactTxtView.setText(parentData!!.contact)
            firstNameTxtView.setText(parentData!!.firstName)
            lastNameTxtView.setText(parentData!!.lastName)
            cityTxtView.setText(parentData!!.city)
            jobStatusTxtView.setText(parentData!!.jobStatus)
            ageTxtView.setText(parentData!!.age)
            genderTxtView.setText(parentData!!.gender)
            jobTypeTxtView.setText(parentData!!.jobType)

        }
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(parentData!!.photo).thumbnail(Glide.with(this).load(parentData!!.thumbnail)).into(binding.imageView)
    }

    override fun onResume() {
        super.onResume()
        parentData = ApplicationClass.documentSnapshot!!.toObject(ParentData::class.java)
        parentData!!.id = ApplicationClass.documentSnapshot!!.id

        setDefaultValues()
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private fun showProgress(show: Boolean) {
        if (show) {
            Toasty.info(requireContext(), "loading...")
        } else {
            Toasty.info(requireContext(), "finished loading")
        }
    }
}
