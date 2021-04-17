package com.justice.schoolmanagement.presentation.ui.parent

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.example.edward.nyansapo.wrappers.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
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

    private val TAG = "ParentDetailsFragment"


    lateinit var parentData: ParentData
    private lateinit var binding: FragmentParentDetailsBinding
    private val viewModel: ParentDetailsViewModel by viewModels()
    private val navArgs: ParentDetailsFragmentArgs by navArgs()

    @Inject
    lateinit var requestManager: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding = FragmentParentDetailsBinding.bind(view)
        initProgressBar()
        subScribeToObservers()

    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun subScribeToObservers() {


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.currentParentFlow.collect {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        viewModel.setCurrentSnapshot(it.data!!)
                        parentData = it.data?.toObject(ParentData::class.java)!!
                        setDefaultValues()
                        setOnClickListeners()
                        setImageViewClickListeners()

                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToastInfo("Error: ${it.exception?.message}")

                    }
                    Resource.Status.EMPTY -> {
                        showProgress(false)
                        Log.d(TAG, "subScribeToObservers: document does not exit")
                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.parentDetailsEvent.collect {
                when (it) {
                    is Event.ParentDelete -> {
                        deleteFromDatabase(it.parentSnapshot)
                    }
                    is Event.ParentEdit -> {
                        val parent = it.parentSnapshot.toObject(ParentData::class.java)!!
                        findNavController().navigate(ParentDetailsFragmentDirections.actionParentDetailsFragmentToEditParentFragment(parent))
                    }
                    is Event.ParentCall -> {

                        startCall(it.number)

                    }
                    is Event.ParentEmail -> {
                        startEmailing(it.email)

                    }
                }

            }

        }


        viewLifecycleOwner.lifecycleScope.launchWhenResumed {


            viewModel.deleteStatus.collect {

                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        findNavController().popBackStack()

                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        showToastInfo(it.exception?.message!!)

                    }

                }

            }
        }
    }

    private fun startEmailing(email: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/html"
        val email = arrayOf(email)
        intent.putExtra(Intent.EXTRA_EMAIL, email)
        startActivity(Intent.createChooser(intent, "Choose app to use for sending Email"))
    }

    private fun startCall(number: String) {

        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)

    }

    private fun setImageViewClickListeners() {
        binding.callImageView.setOnClickListener {

            viewModel.setEvent(Event.ParentCall(parentData.contact))


        }
        binding.emailImageView.setOnClickListener {

            viewModel.setEvent(Event.ParentEmail(parentData.email))


        }
    }

    private fun setOnClickListeners() {
        binding.deleteTxtView.setOnClickListener {
            viewModel.setEvent(Event.ParentDelete(viewModel.currentSnapshot.value!!))
        }
        binding.editTxtView.setOnClickListener {
            viewModel.setEvent(Event.ParentEdit(viewModel.currentSnapshot.value!!))
        }
    }

    private fun deleteFromDatabase(snapshot: DocumentSnapshot) {
        MaterialAlertDialogBuilder(requireContext()).setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", null).setPositiveButton("yes") { dialog, which -> deleteParentPhoto(snapshot) }.show()
    }

    private fun deleteParentPhoto(snapshot: DocumentSnapshot) {

        showProgress(true)
        /**
         * ACTIVITY EXITING BEFORE DELETION OF PHOTO IS COMPLETE MAY CAUSE CRASH OF THE PROGRAM//////////
         *
         */
        FirebaseStorage.getInstance().getReferenceFromUrl(parentData!!.photo).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), "Photo Deleted", Toast.LENGTH_SHORT).show()
                deleteParentMetaData(snapshot)
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteParentMetaData(snapshot: DocumentSnapshot) {
        snapshot.reference.delete().addOnCompleteListener { task ->
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


    sealed class Event {
        data class ParentDelete(val parentSnapshot: DocumentSnapshot) : Event()
        data class ParentEdit(val parentSnapshot: DocumentSnapshot) : Event()
        data class ParentCall(val number: String) : Event()
        data class ParentEmail(val email: String) : Event()

    }
}
