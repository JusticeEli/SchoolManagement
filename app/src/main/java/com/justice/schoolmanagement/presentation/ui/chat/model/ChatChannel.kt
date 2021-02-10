package com.justice.schoolmanagement.presentation.ui.chat.model


data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
}