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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentParentDetailsBinding
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ParentDetailsFragment : Fragment(R.layout.fragment_parent_details) {

    private var parentData: ParentData? = null
    private lateinit var binding: FragmentParentDetailsBinding
    private val viewModel: ParentViewModel by viewModels()

    @Inject
    lateinit var requestManager: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentParentDetailsBinding.bind(view)
        parentData = viewModel.currentParent.value?.toObject(ParentData::class.java)
        initProgressBar()
        setDefaultValues()
        setOnClickListeners()
        setImageViewClickListeners()
        subScribeToObservers()

    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.parentChannelEvents.collect {
                when(it){
                    is ParentsFragment.Event.ParentDelete->{
                        deleteFromDatabase()
                    }
                    is ParentsFragment.Event.ParentEdit->{
                        findNavController().navigate(R.id.action_parentDetailsFragment_to_editParentFragment)

                    }
                }

            }

        }
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
        binding.deleteTxtView.setOnClickListener {
            viewModel.setEvent(ParentsFragment.Event.ParentDelete(viewModel.currentParent.value!!))
        }
        binding.editTxtView.setOnClickListener {
            viewModel.setEvent(ParentsFragment.Event.ParentEdit(viewModel.currentParent.value!!))
         }
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
        viewModel.currentParent.value!!.reference.delete().addOnCompleteListener { task ->
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

       requestManager.load(parentData!!.photo).thumbnail(requestManager.load(parentData!!.thumbnail)).into(binding.imageView)
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
