package com.justice.schoolmanagement.presentation.ui.chat.model



data class User(val name: String,
                val bio: String,
                val profilePicturePath: String?,
                val registrationTokens: MutableList<String>) {
    constructor(): this("", "", null, mutableListOf())
}