package com.justice.schoolmanagement.presentation.ui.chat

import android.app.Activity
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentChatBinding
import com.justice.schoolmanagement.presentation.ui.chat.model.ImageMessage
import com.justice.schoolmanagement.presentation.ui.chat.model.Message
import com.justice.schoolmanagement.presentation.ui.chat.model.TextMessage
import com.justice.schoolmanagement.utils.FirebaseUtil
import com.justice.schoolmanagement.utils.Resource
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val TAG = "ChatFragment"

    @Inject
    lateinit var requestManager: RequestManager
    private val viewModel: ChatViewModel by viewModels()

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val navArgs: ChatFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)
        Log.d(TAG, "onViewCreated:teacherData:${navArgs.teacherData} ")
        initProgressBar()
        subScribeToObservers()
        initRecyclerView()


    }


    private fun subScribeToObservers() {

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {
                viewModel.getChannelId.collect {
                    Log.d(TAG, "subScribeToObservers: getChannelId:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            viewModel.setEvent(Event.ReceivedChannelID(it.data!!))
                            setOnClickListeners()
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                        }


                    }
                }
            }
            launch {
                viewModel.getChats.collect {
                    Log.d(TAG, "subScribeToObservers: getChats:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            updateMessages(it.data!!)
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)

                        }
                        Resource.Status.EMPTY -> {
                            showProgress(false)

                        }

                    }
                }
            }
            launch {
                viewModel.uploadMessageImageStatus.collect {
                    Log.d(TAG, "subScribeToObservers: uploadMessageImageStatus:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            sendMessage(it.data!!)
                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)

                        }
                        Resource.Status.EMPTY -> {
                            showProgress(false)

                        }

                    }
                }
            }
            launch {
                viewModel.getCurrentUser.collect {
                    Log.d(TAG, "subScribeToObservers: getCurrentUser:${it.status.name}")
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)
                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)
                            viewModel.setCurrentUser(it.data!!)
                            viewModel.setOtherTeacher(navArgs.teacherData)
                             viewModel.setEvent(Event.GetOrCreateChatChannel(navArgs.teacherData.id!!))

                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)

                        }
                        Resource.Status.EMPTY -> {
                            showProgress(false)

                        }

                    }
                }
            }
            viewModel.sendMessageStatus.collect {
                Log.d(TAG, "subScribeToObservers: sendMessageStatus:${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {
                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                    }
                    Resource.Status.ERROR -> {

                    }
                    Resource.Status.EMPTY -> {

                    }

                }
            }
        }

    }

    private fun setOnClickListeners() {

        binding.imvSendText.setOnClickListener {
            val messageToSend =
                TextMessage(
                    text = binding.editTextMessage.text.toString(),
                    Calendar.getInstance().time,
                    viewModel.currentUserFlow.value.id!!,
                    viewModel.otherTeacherFlow.value.id!!,
                    viewModel.currentUserFlow.value.fullName
                )
            Log.d(TAG, "setOnClickListeners: messageToSend:$messageToSend")
            binding.editTextMessage.setText("")
            viewModel.setEvent(Event.SendMessage(messageToSend))
        }
        binding.fabSendImage.setOnClickListener {
            choosePhoto()

        }

    }

    private fun sendMessage(imagePath: String) {
        val messageToSend =
            ImageMessage(
                imagePath, Calendar.getInstance().time,
                FirebaseUtil.getUid(),
                navArgs.teacherData.id!!, viewModel.currentUserFlow.value.fullName
            )
        viewModel.setEvent(Event.SendMessage(messageToSend))
    }

    private fun updateMessages(data: List<DocumentSnapshot>) {
        Log.d(TAG, "updateMessages: ")
        chatAdapter.submitList(data)
        binding.recyclerView.smoothScrollToPosition(chatAdapter.itemCount);


    }

    private fun choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(requireContext(), this);
    }

    private fun initRecyclerView() {
        chatAdapter = ChatAdapter(requestManager)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter

        }
        binding.recyclerView.addOnLayoutChangeListener(View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (chatAdapter != null) {
                if (bottom < oldBottom) {
                    binding.recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1)
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var uri: Uri? = null
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                uri = result.uri
                viewModel.setEvent(Event.UploadMessageImage(uri!!))

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.e(TAG, "onActivityResult: ",error)
            }
        }

    }


    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
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
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
        data class ReceivedChannelID(val channelId: String) : Event()
        data class UploadMessageImage(val uri: Uri) : Event()
        data class SendMessage(val message: Message) : Event()
        data class GetOrCreateChatChannel(val otherUserId: String) : Event()

    }
}
