package com.justice.schoolmanagement.presentation.ui.blog

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.wrappers.Resource
import com.justice.schoolmanagement.presentation.ui.blog.model.Blog
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddBlogViewModel @ViewModelInject constructor(private val repository: BlogRepository) : ViewModel() {
    private val _addBlogEvent = Channel<AddBlogFragment.Event>()
    val addBlogEvent = _addBlogEvent.receiveAsFlow()


    private val _addBlogStatus = Channel<Resource<Blog>>()
    val addBlogStatus = _addBlogStatus.receiveAsFlow()

    fun setEvent(event: AddBlogFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is AddBlogFragment.Event.SubmitClicked -> {

                    submitClicked(event.blog)

                }
            }
        }
    }

    private suspend fun submitClicked(blog: Blog) {
        if (fieldsIsEmpty(blog)) {
            _addBlogStatus.send(Resource.error(Exception("Please Fill The Description")))
            return
        }
        trimBlog(blog)
        putPhotoInDatabase(blog)
    }

    private suspend fun putPhotoInDatabase(blog: Blog) {
        repository.putPhotoInDatabase(blog.uri!!).collect {
            when (it.status) {
                Resource.Status.LOADING -> {
                    _addBlogStatus.send(Resource.loading(""))
                }
                Resource.Status.SUCCESS -> {
                    putBlogIntoDatabase(blog, it.data!!)

                }
                Resource.Status.ERROR -> {
                    _addBlogStatus.send(Resource.error(it.exception))

                }
            }
        }
    }

    private suspend fun putBlogIntoDatabase(blog: Blog, downloadLink: String) {
        blog.uri = null
        blog.photo = downloadLink
        blog.userId = FirebaseUtil.getUid()

        repository.putBlogIntoDatabase(blog).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    _addBlogStatus.send(it)
                }
                Resource.Status.ERROR -> {
                    _addBlogStatus.send(it)

                }
            }
        }

    }

    private fun trimBlog(blog: Blog) {
        blog.description = blog.description.trim()
    }

    private fun fieldsIsEmpty(blog: Blog): Boolean {
        return blog.description.isBlank()
    }

}