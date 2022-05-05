package com.justice.schoolmanagement.presentation.ui.chat.model


import java.util.*


object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
}

const val FIELD_TIME="time"
interface Message {
    val time: Date
    val senderId: String
    val recipientId: String
    val senderName: String
    val type: String
}