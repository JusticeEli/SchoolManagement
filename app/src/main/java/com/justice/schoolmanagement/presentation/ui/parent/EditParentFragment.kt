package com.justice.schoolmanagement.presentation.ui.parent

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.*
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentEditParentBinding
import com.justice.schoolmanagement.presentation.SchoolApplication
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import com.justice.schoolmanagement.presentation.utils.Constants
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.fragment_add_parent.*
import kotlinx.coroutines.flow.collect
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EditParentFragment : Fragment(R.layout.fragment_edit_parent) {

    private val TAG = "EditParentFragment"

    @Inject
    lateinit var requestManager: RequestManager
    private var parentData: ParentData? = null
    private var uri: Uri? = null
    private var photoChanged = false
    lateinit var progressBar: ProgressBar
    lateinit var binding: FragmentEditParentBinding
    private val viewModel by viewModels<EditParentViewModel>()
    private val navArgs: EditParentFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditParentBinding.bind(view)
        initProgressBar()
        initAdapters()
        subScribeToObservers()

        Log.d(TAG, "onViewCreated: parentData:${navArgs.parent}")

    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getParentToEdit.collect {
                Log.d(TAG, "subScribeToObservers: status:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        Log.d(TAG, "subScribeToObservers: success loading data")
                        showProgress(false)
                        parentData = it.data?.toObject(ParentData::class.java)!!

                        setDefaulValues()
                        setOnClickListeners()
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

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.editParentStatus.collect {
                Log.d(TAG, "subsribeToObservers: editParentStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                        Log.d(TAG, "subsribeToObservers: LOADING:${it.message}")
                    }
                    Resource.Status.SUCCESS -> {
                        showProgress(false)
                    }
                    Resource.Status.ERROR -> {
                        showProgress(false)
                        Log.d(TAG, "subsribeToObservers: Error:${it.exception?.message}")
                    }
                    Resource.Status.EMPTY -> {
                        showToastInfo("Please Fill All Fields")
                    }

                }
            }
        }
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(requireContext(), this);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                uri = result.uri
                photoChanged = true
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.mipmap.place_holder)
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(binding.imageView)
    }

    private fun initAdapters() {
        val jobStatus = requireActivity().resources.getStringArray(R.array.job_status)
        val jobStatusAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, jobStatus)
        binding.jobStatusSpinner.setAdapter(jobStatusAdapter)
        val cities = requireActivity().resources.getStringArray(R.array.cities)
        val cityAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, cities)
        binding.cityEdtTxt.setAdapter<ArrayAdapter<String>>(cityAdapter)
        val jobType = requireActivity().resources.getStringArray(R.array.jobType)
        val jobTypeAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, jobType)
        binding.jobTypeEdtTxt.setAdapter<ArrayAdapter<String>>(jobTypeAdapter)
    }


    private fun setDefaulValues() {
        Log.d(TAG, "setDefaulValues: parentData:$parentData")
        binding.apply {

            firstNameEdtTxt.setText(parentData!!.firstName)
            lastNameEdtTxt.setText(parentData!!.lastName)
            emailEdtTxt.setText(parentData!!.email)
            setJobStatusDefaultValue()
            cityEdtTxt.setText(parentData!!.city)
            jobTypeEdtTxt.setText(parentData!!.jobType)
            ageEdtTxt.setText(parentData!!.age)
            setDefaultValueForRadioBtn()
            contactEdtTxt.setText(parentData!!.contact)
        }
        try {
            uri = Uri.parse(parentData!!.photo)
            requestManager.load(parentData!!.photo).thumbnail(Glide.with(this).load(parentData!!.thumbnail)).into(binding.imageView)

        } catch (e: Exception) {
            e.printStackTrace()
            showToastInfo("Error: ${e.message}")
        }

    }

    private fun setJobStatusDefaultValue() {
        binding.apply {
            when (parentData!!.jobStatus) {
                "Employed" -> jobStatusSpinner.setSelection(0)
                "Unemployed" -> jobStatusSpinner.setSelection(1)
                "Retired" -> jobStatusSpinner.setSelection(2)
            }
        }

    }

    private fun setDefaultValueForRadioBtn() {
        binding.apply {
            when (parentData!!.gender) {
                "Male" -> maleRadioBtn.setChecked(true)
                "Female" -> femaleRadioBtn.setChecked(true)
                "Other" -> otherRadioBtn.setChecked(true)
            }
        }

    }

    private fun contactEdtTxtFormatIsCorrect(): Boolean {
        val contact: String = binding.contactEdtTxt.getText().toString().trim { it <= ' ' }
        if (!contact.startsWith("07")) {
            Toasty.error(requireContext(), "Contact Must start with 07 !!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (contact.length != 10) {
            Toasty.error(requireContext(), "Contact Must have 10 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun getParentObject(): ParentData {
        val firstName = firstNameEdtTxt.text.toString()
        val lastName = lastNameEdtTxt.text.toString()
        val fullName = "$firstName $lastName"

        val email = emailEdtTxt.text.toString()
        val city = cityEdtTxt.text.toString()
        val contact = contactEdtTxt.text.toString()
        val age = ageEdtTxt.text.toString()
        val jobType = jobTypeEdtTxt.text.toString()
        val jobStatus = jobStatusSpinner.getSelectedItem().toString()
        val gender = getSelectedRadioBtn()!!


        val parent = ParentData()
        parent.firstName = firstName
        parent.lastName = lastName
        parent.fullName = "${parent.firstName} ${parent.lastName}"
        parent.email = email
        parent.city = city
        parent.contact = contact
        parent.age = age
        parent.jobType = jobType
        parent.jobStatus = jobStatus
        parent.gender = gender

        //not uploaded into firebase
        parent.uri = uri

        return parent
    }

    private fun setOnClickListeners() {
        binding.imageView.setOnClickListener { choosePhoto() }
        binding.addPhotoBtn.setOnClickListener { choosePhoto() }
        binding.submitBtn.setOnClickListener {
            Log.d(TAG, "setOnClickListeners: submitBtn Clicked")
            if (uri == null) {
                Toasty.error(requireContext(), "Please choose a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parent = getParentObject()
            parent.photo = parentData?.photo ?: ""
            parent.thumbnail = parentData?.thumbnail ?: ""
            viewModel.setEvent(ParentAddEditViewModel.Event.ParentEditSubmitClicked(parent!!, photoChanged))

        }
        binding.contactEdtTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                binding.apply {
                    if (contactEdtTxt.length() == 10) {
                        ageEdtTxt.requestFocus()
                    }
                    if (contactEdtTxt.length() > 10) {
                        contactEdtTxt.setError("Contact Must have 10 characters")
                    }
                }

            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun fieldsAreEmpty(): Boolean {
        binding.apply {
            return if (firstNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || lastNameEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || emailEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || cityEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || contactEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || ageEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || jobTypeEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty()) {
                true
            } else false
        }

    }


    private fun getDataFromEdtTxtAndSaveIntoDatabase() {
        // TODO: 11-Feb-20  extract a photo from the addPhoto button

        binding.apply {
            parentData!!.fullName = firstNameEdtTxt.getText().toString().trim { it <= ' ' } + " " + lastNameEdtTxt.getText().toString().trim { it <= ' ' }
            parentData!!.contact = contactEdtTxt.getText().toString().trim { it <= ' ' }
            parentData!!.firstName = firstNameEdtTxt.getText().toString().trim { it <= ' ' }
            parentData!!.lastName = lastNameEdtTxt.getText().toString().trim { it <= ' ' }
            parentData!!.city = cityEdtTxt.getText().toString().trim { it <= ' ' }
            parentData!!.jobStatus = jobStatusSpinner.getSelectedItem().toString().trim { it <= ' ' }
            parentData!!.age = ageEdtTxt.getText().toString()
            parentData!!.gender = getSelectedRadioBtn()!!
            parentData!!.jobType = jobTypeEdtTxt.getText().toString().trim { it <= ' ' }
            parentData!!.email = emailEdtTxt.getText().toString().trim { it <= ' ' }
        }

        if (photoChanged) {
            showProgress(true)
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(parentData!!.photo)
            val uploadTask = ref.putFile(uri!!)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                // Continue with the task to get the download URL
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    parentData!!.photo = downloadUri.toString()
                    uploadThumbnail()
                    Toasty.success(requireContext(), "Photo Uploaded", Toast.LENGTH_SHORT).show()
                } else {
                    val error = task.exception!!.message
                    Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }

            /////////////////////////////////////////////
        } else {
            putDataInDatabase()
        }
    }

    private fun uploadThumbnail() {
        val thumbnail: Uri
        var compressedImgFile: File? = null
        try {
            compressedImgFile = Compressor(requireActivity()).setCompressFormat(Bitmap.CompressFormat.JPEG).setMaxHeight(10).setMaxWidth(10).setQuality(40).compressToFile(File(uri!!.path))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        thumbnail = Uri.fromFile(compressedImgFile)
        showProgress(true)
        val photoName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS_THUMBNAIL_IMAGES).child(photoName)
        val uploadTask = ref.putFile(thumbnail)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                parentData!!.thumbnail = downloadUri.toString()
                putDataInDatabase()
                Toasty.success(requireContext(), "Thumbnail Uploaded", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }

        }
    }


    private fun putDataInDatabase() {
        showProgress(true)
        SchoolApplication.documentSnapshot!!.reference.set(parentData!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                SchoolApplication.documentSnapshot!!.reference.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        SchoolApplication.documentSnapshot = task.result
                        Toasty.success(requireContext(), parentData!!.firstName + " Edited Successfully", Toast.LENGTH_SHORT).show()
                        Handler().postDelayed(object : Runnable {
                            override fun run() {
                                showProgress(false)
                                findNavController().popBackStack()
                            }

                        }, 100)
                    } else {
                        val error = task.exception!!.message
                        Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun getSelectedRadioBtn(): String? {
        when (binding.genderRadioGroup.getCheckedRadioButtonId()) {
            R.id.maleRadioBtn -> return "Male"
            R.id.femaleRadioBtn -> return "Female"
            R.id.otherRadioBtn -> return "Other"
        }
        return null
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

        llParam = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT)
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
