package com.justice.schoolmanagement.presentation.ui.blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentAddBlogBinding
import com.justice.schoolmanagement.presentation.ui.blog.model.Blog
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import es.dmoral.toasty.Toasty
import java.util.*

class AddBlogFragment : Fragment(R.layout.fragment_add_blog) {
    private var blog: Blog? = null
    private var uri: Uri? = null
    private var downLoadLink: String? = null
    lateinit var binding: FragmentAddBlogBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddBlogBinding.bind(view)

        setOnClickListeners()

    }

    private fun setOnClickListeners() {
        binding.postImageView.setOnClickListener(View.OnClickListener { // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(requireContext(), this);
        })
        binding.submitBtn.setOnClickListener(View.OnClickListener { putPhotoInDatabase() })
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private fun showProgress(show: Boolean) {
        if (show) {
            Toasty.info(requireContext(), "loading...")
        } else {
            Toasty.info(requireContext(), "finished loading")
        }
    }


private fun putBlogIntoDatabase() {
    blog = Blog(FirebaseAuth.getInstance().uid, binding.descriptionEdtTxt.getText().toString().trim { it <= ' ' }, downLoadLink, downLoadLink, Date())
    showProgress(true)
    FirebaseFirestore.getInstance().collection("blogs").add(blog!!).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Toasty.success(requireContext(), "Blog Added", Toast.LENGTH_SHORT).show()
       findNavController().popBackStack()
        } else {
            val error = task.exception!!.message
            Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
        }
        showProgress(false)
    }
}

private fun putPhotoInDatabase() {
    if (binding.descriptionEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || uri == null) {
        Toasty.error(requireContext(), "Please Fill All Fields", Toast.LENGTH_SHORT).show()
        return
    }
    val name = UUID.randomUUID().toString()
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    showProgress(true)
    val ref = FirebaseStorage.getInstance().getReference("blogs_images").child(name)
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
            downLoadLink = downloadUri.toString()
            putBlogIntoDatabase()
            Toasty.success(requireContext(), "Photo Uploaded", Toast.LENGTH_SHORT).show()
        } else {
            val error = task.exception!!.message
            Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
        }
        showProgress(false)
    }

    /////////////////////////////////////////////
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        val result = CropImage.getActivityResult(data)
        if (resultCode == Activity.RESULT_OK) {
            uri = result.uri
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            val error = result.error
        }
    }
    val requestOptions = RequestOptions()
    requestOptions.centerCrop()
    Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(binding.postImageView)
}

}