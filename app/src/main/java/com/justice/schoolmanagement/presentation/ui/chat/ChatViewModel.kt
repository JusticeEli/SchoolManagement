package com.justice.schoolmanagement.presentation.ui.chat

import android.net.Uri
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.chat.model.ImageMessage
import com.justice.schoolmanagement.presentation.ui.chat.model.Message
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel @ViewModelInject constructor(private val repository: ChatRepository) :
    ViewModel() {

    private val TAG = "ChatViewModel"

    val _getChannelId = Channel<Resource<String>>()
    val getChannelId = _getChannelId.receiveAsFlow()


    private val _currentUserFlow = MutableStateFlow(TeacherData())
    val currentUserFlow = _currentUserFlow as StateFlow<TeacherData>
    fun setCurrentUser(teacherData: TeacherData) {
        _currentUserFlow.value = teacherData
    }

    private val _otherTeacherFlow = MutableStateFlow(TeacherData())
    val otherTeacherFlow = _otherTeacherFlow as StateFlow<TeacherData>
    fun setOtherTeacher(teacherData: TeacherData) {
        _otherTeacherFlow.value = teacherData
    }

    val getCurrentUser = repository.getCurrentUser()

    fun setEvent(event: ChatFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is ChatFragment.Event.GetOrCreateChatChannel -> {
                    repository.getChannelId(event.otherUserId).collect {
                        _getChannelId.send(it)
                    }
                }
                is ChatFragment.Event.ReceivedChannelID -> {
                    channelIdReceived(event.channelId)
                }
                is ChatFragment.Event.SendMessage -> {
                    sentMessage(event.message)
                }
                is ChatFragment.Event.UploadMessageImage -> {
                    sentImageMessage(event.uri)
                }

            }
        }
    }


    private val _currentChannelIdFlow = MutableStateFlow("")
    val currentChannelIdFlow = _currentChannelIdFlow as StateFlow<String>


    private val _sendMessageStatus = Channel<Resource<String>>()
    val sendMessageStatus = _sendMessageStatus.receiveAsFlow()

    private suspend fun sentMessage(message: Message) {
        Log.d(TAG, "sentMessage: message:$message")
        repository.sendMessage(message, currentChannelIdFlow.value).collect {
            _sendMessageStatus.send(it)
            Log.d(TAG, "sentMessage: status:${it.status.name}")
        }
    }

    private val _uploadMessageImageStatus = Channel<Resource<String>>()
    val uploadMessageImageStatus = _uploadMessageImageStatus.receiveAsFlow()

    private suspend fun sentImageMessage(uri: Uri) {
        repository.uploadMessageImage(uri).collect {
            _uploadMessageImageStatus.send(it)
            Log.d(TAG, "sentImageMessage: updloadMessageImage:${it.status.name}")

            when (it.status) {
                Resource.Status.SUCCESS -> {

                    val messageToSend =
                        ImageMessage(
                            imagePath = it.data!!,
                            Calendar.getInstance().time,
                            currentUserFlow.value.id!!,
                            otherTeacherFlow.value.id!!,
                            currentUserFlow.value.fullName
                        )
                    Log.d(TAG, "sentImageMessage: messageToSend:$messageToSend")
                    sentMessage(messageToSend)


                }
            }

        }
    }

    private val _getChats = Channel<Resource<List<DocumentSnapshot>>>()
    val getChats = _getChats.receiveAsFlow()
    private suspend fun channelIdReceived(channelId: String) {
        Log.d(TAG, "channelIdReceived: channelId:$channelId")

        _currentChannelIdFlow.value = channelId
        repository.getChats(channelId).collect {
            _getChats.send(it)
            Log.d(TAG, "channelIdReceived: getChats:${it.status.name}")

        }
    }

    fun setOtherUserId(teacherData: TeacherData) {
        TODO("Not yet implemented")
    }
}