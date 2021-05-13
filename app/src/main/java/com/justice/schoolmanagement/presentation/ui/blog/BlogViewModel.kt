package com.justice.schoolmanagement.presentation.ui.blog

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class BlogViewModel @ViewModelInject constructor(private val repository: BlogRepository) : ViewModel() {

    val getBlogs = repository.getBlogs()

    private val _blogEvents = Channel<BlogFragment.Event>()
    val blogEvents = _blogEvents.receiveAsFlow()

    fun setEvent(event: BlogFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is BlogFragment.Event.AddClicked -> {
                    _blogEvents.send(BlogFragment.Event.AddClicked)
                }
                is BlogFragment.Event.SwipedBlog -> {
                    _blogEvents.send(BlogFragment.Event.SwipedBlog(event.snapshot))
                }
                is BlogFragment.Event.DeleteConfirmed -> {
                    deleteBlog(event.snapshot)

                }
            }
        }
    }

    private val _deleteBlogStatus = Channel<Resource<DocumentSnapshot>>()
    val deleteBlogStatus = _deleteBlogStatus.receiveAsFlow()
    private suspend fun deleteBlog(snapshot: DocumentSnapshot) {
       repository.deleteBlog(snapshot).collect {
              _deleteBlogStatus.send(it)
       }
    }




}