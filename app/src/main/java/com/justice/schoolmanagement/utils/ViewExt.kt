package com.justice.schoolmanagement.utils

import android.util.Log
import androidx.appcompat.widget.SearchView

import java.text.SimpleDateFormat
import java.util.*

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}


val <T> T.exhaustive: T
    get() = this




val Date.formatDate get() = SimpleDateFormat("dd/MM/yyyy").format(this)

val String.cleanString
    get() =
        this.replace("/", "_")


fun String.toIntOrZero():Int{
    try {
        return   this.toInt()
    }catch (e:Exception)
    {
        Log.e("ViewExt", "toIntOrZero: ", e)
    }

    return 0
}
