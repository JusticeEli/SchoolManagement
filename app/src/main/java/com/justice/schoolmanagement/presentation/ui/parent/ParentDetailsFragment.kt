package com.justice.schoolmanagement.presentation.ui.parent

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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
        initProgressBar()
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
            findNavController().navigate(R.id.action_parentDetailsFragment_to_editParentFragment)
        })
    }

    private fun deleteFromDatabase() {
        MaterialAlertDialogBuilder(requireContext()).setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", null).setPositiveButton("yes") { dialog, which -> deleteParentPhoto() }.show()
    }

    private fun deleteParentPhoto() {

        showProgress(true)
        /**
         * ACTIVITY EXITING BEFORE DELETION OF PHOTO IS COMPLETE MAY CAUSE CRASH OF THE PROGRAM//////////
         *
         */
        FirebaseStorage.getInstance().getReferenceFromUrl(parentData!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), "Photo Deleted", Toast.LENGTH_SHORT).show()
                deleteParentMetaData()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteParentMetaData() {
        ApplicationClass.documentSnapshot!!.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), parentData!!.firstName + " Parent Removed Successfully", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
            findNavController().popBackStack()

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
    lateinit var dialog: AlertDialog

    private fun showProgress(show: Boolean) {

        if (show) {
            dialog.show()

        } else {
            dialog.dismiss()

        }

    }

    private fun initProgressBar() {

        dialog = setProgressDialog(requireContext(), "Loading..")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    fun setProgressDialog(context: Context, message: String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    //end progressbar
}
