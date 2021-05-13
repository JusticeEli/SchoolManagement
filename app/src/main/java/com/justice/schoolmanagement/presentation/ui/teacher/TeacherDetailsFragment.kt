package com.justice.schoolmanagement.presentation.ui.teacher

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentTeacherDetailsBinding
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class TeacherDetailsFragment : Fragment(R.layout.fragment_teacher_details) {

    private val TAG = "TeacherDetailsFragment"


    lateinit var binding: FragmentTeacherDetailsBinding
    private val viewModel by viewModels<TeacherDetailViewModel>()
    private val navArgs by navArgs<TeacherDetailsFragmentArgs>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: teacher:${navArgs.teacherData}")
        binding = FragmentTeacherDetailsBinding.bind(view)
        initProgressBar()
        subScribeToObservers()
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getTeacher.collect {
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                        viewModel.setCurrentSnapshot(it.data!!)
                        val teacherData = it.data.toObject(TeacherData::class.java)!!
                        setDefaultValues(teacherData)
                        setOnClickListeners()
                        setImageViewClickListeners(teacherData)

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
            viewModel.teacherDetailsEvent.collect {
                when (it) {
                    is Event.TeacherDelete -> {
                        deleteTeacherDataFromDatabase(it.snapshot)
                    }
                    is Event.TeacherEdit -> {
                        val teacher = it.snapshot.toObject(TeacherData::class.java)!!
                        findNavController().navigate(TeacherDetailsFragmentDirections.actionTeacherDetailsFragmentToEditTeacherFragment(teacher))
                    }
                    is Event.TeacherChat -> {
                        Log.d(TAG, "subScribeToObservers: teacherchat")
                        val teacher = it.snapshot.toObject(TeacherData::class.java)!!
                        findNavController().navigate(TeacherDetailsFragmentDirections.actionTeacherDetailsFragmentToChatFragment(teacher))
                    }
                    is Event.TeacherCall -> {
                        startCall(it.number)
                    }
                    is Event.TeacherEmail -> {
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

    private fun setImageViewClickListeners(teacherData: TeacherData) {
        binding.callImageView.setOnClickListener {

            viewModel.setEvent(TeacherDetailsFragment.Event.TeacherCall(teacherData.contact))


        }
        binding.emailImageView.setOnClickListener {

            viewModel.setEvent(TeacherDetailsFragment.Event.TeacherEmail(teacherData.email))


        }
    }

    private fun setOnClickListeners() {
        binding.deleteTxtView.setOnClickListener {
            viewModel.setEvent(TeacherDetailsFragment.Event.TeacherDelete(viewModel.currentSnapshot.value!!))
        }
        binding.editTxtView.setOnClickListener {
            viewModel.setEvent(TeacherDetailsFragment.Event.TeacherEdit(viewModel.currentSnapshot.value!!))
        }
        binding.chatTxtView.setOnClickListener {
            viewModel.setEvent(TeacherDetailsFragment.Event.TeacherChat(viewModel.currentSnapshot.value!!))
        }
    }


    private fun deleteTeacherDataFromDatabase(snapshot: DocumentSnapshot) {
        MaterialAlertDialogBuilder(requireContext()).setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", null).setPositiveButton("yes") { dialog, which ->
            viewModel.setEvent(Event.TeacherDeleteConfirmed(snapshot))
        }.show()
    }


    private fun setDefaultValues(teacherData: TeacherData) {
        binding.apply {
            teacherNameTxtView.setText(teacherData!!.firstName + " " + teacherData!!.lastName)
            teacherSubjectTxtView.setText(teacherData!!.subject)
            firstNameTxtView.setText(teacherData!!.firstName)
            lastNameTxtView.setText(teacherData!!.lastName)
            cityTxtView.setText(teacherData!!.city)
            degreeTxtView.setText(teacherData!!.degree)
            ageTxtView.setText(teacherData!!.age)
            genderTxtView.setText(teacherData!!.gender)
            salaryTxtView.setText(teacherData!!.salary)
            emailTxtView.setText(teacherData!!.email)
            contactTxtView.setText(teacherData!!.contact)
            val requestOptions = RequestOptions()
            requestOptions.placeholder(R.mipmap.place_holder)
            requestOptions.centerCrop()
            Glide.with(requireContext()).applyDefaultRequestOptions(requestOptions).load(teacherData!!.photo).thumbnail(Glide.with(requireContext()).load(teacherData!!.thumbnail)).into(imageView)

        }

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
        data class TeacherDelete(val snapshot: DocumentSnapshot) : Event()
        data class TeacherDeleteConfirmed(val snapshot: DocumentSnapshot) : Event()
        data class TeacherEdit(val snapshot: DocumentSnapshot) : Event()
        data class TeacherChat(val snapshot: DocumentSnapshot) : Event()
        data class TeacherCall(val number: String) : Event()
        data class TeacherEmail(val email: String) : Event()

    }
}