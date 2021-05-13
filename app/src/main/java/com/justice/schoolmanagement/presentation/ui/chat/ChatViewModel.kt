package com.justice.schoolmanagement.presentation.ui.chat

import android.net.Uri
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.chat.model.ImageMessage
import com.justice.schoolmanagement.presentation.ui.teacher.model.TEACHER_DATA_ARGS
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ChatViewModel @ViewModelInject constructor(private val repository: ChatRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val TAG = "ChatViewModel"
    private val _channelIdFlow = MutableStateFlow("")
    val channelIdFlow = _channelIdFlow as StateFlow<String>
    fun setChannelId(channelId: String) {
        _channelIdFlow.value = channelId
    }
    val getChannelId = repository.getChannelId(savedStateHandle.get<TeacherData>(TEACHER_DATA_ARGS)!!.id!!)

    private val _currentUserFlow = MutableStateFlow(TeacherData())
    val currentUserFlow = _currentUserFlow as StateFlow<TeacherData>
    fun setCurrentUser(teacherData: TeacherData) {
        _currentUserFlow.value = teacherData
    }

    val getCurrentUser = repository.getCurrentUser()

    fun setEvent(event: ChatFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is ChatFragment.Event.ReceivedChannelID -> {
                    channelIdReceived(event.channelId)
                }
                is ChatFragment.Event.SendMessage -> {
                    sentMessage(event.imageMessage)
                }
            }
        }
    }
    private val _sendMessageStatus = Channel<Resource<String>>()
    val sendMessageStatus = _sendMessageStatus.receiveAsFlow()

    private suspend fun sentMessage(imageMessage: ImageMessage) {
        repository.sendMessage(imageMessage,channelIdFlow.value)
    }

    private val _uploadMessageImageStatus = Channel<Resource<String>>()
    val uploadMessageImageStatus = _uploadMessageImageStatus.receiveAsFlow()

    private suspend fun sentImageMessage(uri: Uri) {
        repository.uploadMessageImage(uri).collect {
            Log.d(TAG, "sentImageMessage: updloadMessageImage:${it.status.name}")

        }
    }

    private val _getChats = Channel<Resource<List<DocumentSnapshot>>>()
    val getChats = _getChats.receiveAsFlow()
    private suspend fun channelIdReceived(channelId: String) {
        repository.getChats(channelId).collect {
            Log.d(TAG, "channelIdReceived: getChats:${it.status.name}")
            _getChats.send(it)
        }
    }
}