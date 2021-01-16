package com.justice.schoolmanagement.presentation.ui.teacher

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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentTeacherDetailsBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty

class TeacherDetailsFragment : Fragment(R.layout.fragment_teacher_details) {
    private val TEACHER_REQUEST_CODE = 3
    private val email: String? = null
    private var teacherData: TeacherData? = null

    lateinit var binding: FragmentTeacherDetailsBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTeacherDetailsBinding.bind(view)

        teacherData = ApplicationClass.documentSnapshot!!.toObject(TeacherData::class.java)
        teacherData!!.id = ApplicationClass.documentSnapshot!!.id
        setDefaultValues()
        setOnClickListeners()
        setImageViewClickListeners()
        initProgressBar()
    }

    private fun setImageViewClickListeners() {
        binding.callImageView.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + teacherData!!.contact)
            startActivity(intent)
        })
        binding.emailImageView.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/html"
            val email = arrayOf(teacherData!!.email)
            intent.putExtra(Intent.EXTRA_EMAIL, email)
            startActivity(Intent.createChooser(intent, "Choose app to use for sending Email"))
        })
    }

    private fun setOnClickListeners() {
        binding.deleteTxtView.setOnClickListener(View.OnClickListener { deleteTeacherDataFromDatabase() })
        binding.editTxtView.setOnClickListener(View.OnClickListener {
            findNavController().navigate(R.id.action_teacherDetailsFragment_to_editTeacherFragment)
        })
    }


    override fun onResume() {
        super.onResume()
        teacherData = ApplicationClass.documentSnapshot!!.toObject(TeacherData::class.java)
        teacherData!!.id = ApplicationClass.documentSnapshot!!.id
        setDefaultValues()
    }

    private fun deleteTeacherDataFromDatabase() {
        MaterialAlertDialogBuilder(requireContext()).setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", null).setPositiveButton("yes") { dialog, which -> deleteTeacherPhoto() }.show()
    }

    private fun deleteTeacherPhoto() {
        showProgress(true)
        FirebaseStorage.getInstance().getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS_IMAGES).child(teacherData!!.id + ".jpg").delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), "Photo Deleted", Toast.LENGTH_SHORT).show()
                deleteTeacherMetadata();
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
         }

    }

    private fun deleteTeacherMetadata() {
        ApplicationClass.documentSnapshot!!.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), " Teacher deleted", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            findNavController().popBackStack()

        }
    }

    private fun setDefaultValues() {
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
}