package com.justice.schoolmanagement.presentation.ui.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentChatBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.chat.model.ImageMessage
import com.justice.schoolmanagement.presentation.ui.chat.model.Message
import com.justice.schoolmanagement.presentation.ui.chat.model.TextMessage
import com.justice.schoolmanagement.presentation.ui.chat.util.FirestoreUtil
import com.justice.schoolmanagement.presentation.ui.chat.util.StorageUtil
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.*

class ChatFragment : Fragment(R.layout.fragment_chat) {

      companion object {
              private  const val TAG="ChatFragment"
          }
    private lateinit var currentChannelId: String
    private lateinit var currentUser: TeacherData
    private lateinit var otherUserId: String
    private lateinit var binding: FragmentChatBinding

    lateinit var chatFragmentAdapter: ChatFragmentAdapter
    private lateinit var messagesListenerRegistration: ListenerRegistration

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)
        initRecyclerView()
        FirestoreUtil.getCurrentUser {
            currentUser = it!!.toObject(TeacherData::class.java)!!
        }
        otherUserId = ApplicationClass.documentSnapshot!!.id

        FirestoreUtil.getOrCreateChatChannel(otherUserId) { channelId ->
            currentChannelId = channelId

            messagesListenerRegistration =
                    FirestoreUtil.addChatMessagesListener(channelId, requireContext(), this::updateRecyclerView)
            binding.apply {
                imageViewSend.setOnClickListener {
                    val messageToSend =
                            TextMessage(editTextMessage.text.toString(), Calendar.getInstance().time,
                                    FirebaseAuth.getInstance().currentUser!!.uid,
                                    otherUserId, currentUser.fullName)
                    editTextMessage.setText("")
                    FirestoreUtil.sendMessage(messageToSend, channelId)
                }

                fabSendImage.setOnClickListener {


                    choosePhoto()
                   /* val intent = Intent().apply {
                        type = "image/*"
                        action = Intent.ACTION_GET_CONTENT
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                    }
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)*/

                    */



                }

            }

        }
    }

    private fun choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(requireContext(), this);
    }

    private fun initRecyclerView() {
        chatFragmentAdapter = ChatFragmentAdapter()
        binding.recyclerView.apply {

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())

            adapter = chatFragmentAdapter

        }



        binding.recyclerView.addOnLayoutChangeListener(View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (chatFragmentAdapter != null) {
                if (bottom < oldBottom) {
                    binding.recyclerView.smoothScrollToPosition(chatFragmentAdapter.getItemCount() - 1)
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var uri: Uri?=null
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
         uri = result.uri
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }

            StorageUtil.uploadMessageImage(uri!!) { imagePath ->
                val messageToSend =
                        ImageMessage(imagePath, Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                otherUserId, currentUser.fullName)
                FirestoreUtil.sendMessage(messageToSend, currentChannelId)
            }

    }

    private fun updateRecyclerView(messages: List<Message>) {
        Log.d(TAG, "updateRecyclerView: ${chatFragmentAdapter.itemCount}")
        chatFragmentAdapter.submitList(messages)
        binding.recyclerView.smoothScrollToPosition(chatFragmentAdapter.itemCount);


    }
}
