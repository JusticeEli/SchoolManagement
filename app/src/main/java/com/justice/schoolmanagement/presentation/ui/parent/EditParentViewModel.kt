package com.justice.schoolmanagement.presentation.ui.parent

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class EditParentViewModel @ViewModelInject constructor(private val repository: ParentRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val TAG = "EditParentViewModel"

    fun setEvent(event: EditParentViewModel.Event) {
        viewModelScope.launch {
            when (event) {
                is EditParentViewModel.Event.ParentEditSubmitClicked -> {
                    if (fieldsAreEmpty(event.parent)) {
                        _editParentStatus.send(Resource.empty())
                    } else if (!contactEdtTxtFormatIsCorrect(event.parent)) {

                        _editParentStatus.send(Resource.error(Exception("contact format is incorrect")))
                        /*NO OP*/
                    } else {
                        _editParentStatus.send(Resource.loading("started the uploading parent"))
                        getDataFromEdtTxtAndSaveInDatabase(event.parent, event.photoChanged)
                    }
                }
            }

        }
    }


    val getParentToEdit = repository.getParent(savedStateHandle.get<ParentData>(ParentsFragment.PARENT_ARGS)?.id!!)


    private val _currentSnapshot = MutableLiveData<DocumentSnapshot>()
    val currentSnapshot get() = _currentSnapshot as LiveData<DocumentSnapshot>
    fun setCurrentSnapshot(snapshot: DocumentSnapshot) {
        _currentSnapshot.value = snapshot
    }


    private val _editParentStatus = Channel<Resource<ParentData>>()
    val editParentStatus get() = _editParentStatus.receiveAsFlow()


    private fun fieldsAreEmpty(parentData: ParentData): Boolean {
        return (parentData.firstName.isBlank()
                || parentData.lastName.isBlank()
                || parentData.email.isBlank()
                || parentData.city.isBlank()
                || parentData.contact.isBlank()
                || parentData.age.isBlank()
                || parentData.jobType.isBlank())

    }

    private suspend fun getDataFromEdtTxtAndSaveInDatabase(parentData: ParentData, photoChanged: Boolean = true) {
        Log.d(TAG, "getDataFromEdtTxtAndSaveInDatabase: parentData:$parentData")
        parentData.firstName = parentData.firstName.trim()
        parentData.lastName = parentData.lastName.trim()
        parentData.fullName = "${parentData.firstName.trim()}  ${parentData.lastName}"

        parentData.contact = parentData.contact.trim()
        parentData.city = parentData.city.trim()
        parentData.jobStatus = parentData.jobStatus.trim()
        parentData.age = parentData.age.trim()
        parentData.gender = parentData.gender.trim()
        parentData.jobType = parentData.jobType.trim()
        parentData.email = parentData.email.trim()

        if (photoChanged) {
            putPhotoIntoDatabase(parentData)

        } else {
            putDataIntoDataBase(parentData)
        }

    }

    private suspend fun putPhotoIntoDatabase(parentData: ParentData) {
        Log.d(TAG, "putPhotoIntoDatabase:started parentData:$parentData")
        val photoName = UUID.randomUUID().toString()

        repository.putPhotoIntoDatabase(photoName, parentData.uri!!).collect {
            Log.d(TAG, "putPhotoIntoDatabase: status of uploading photo: ${it.status.name}")
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    viewModelScope.launch {
                        Log.d(TAG, "putPhotoIntoDatabase: success uploading photo")
                        parentData.photo = it.data!!

                        uploadThumbnail(parentData)

                    }
                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "putPhotoIntoDatabase: Error trying trying to upload photo:${it.exception?.message}")
                    it.exception?.printStackTrace()
                }
            }
        }

        ///


    }

    private suspend fun uploadThumbnail(parentData: ParentData) {
        Log.d(TAG, "uploadThumbnail: parentData:$parentData")


        repository.uploadThumbnail(parentData.uri!!).collect {
            Log.d(TAG, "uploadThumbnail: status of uploading thumbnail: ${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    parentData.thumbnail = it.data!!
                    putDataIntoDataBase(parentData)

                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "uploadThumbnail: Error ${it.exception?.message}")

                }
            }
        }


    }

    private suspend fun putDataIntoDataBase(parentData: ParentData) {
        Log.d(TAG, "putDataIntoDataBase: parentData:$parentData")

        parentData.uri = null //must be there since firebase does not know how to handle URI object

        repository.updateDataInDatabase(parentData,currentSnapshot.value!!).collect {
            Log.d(TAG, "putDataIntoDataBase: status is: ${it.status}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                     _editParentStatus.send(it)

                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "putDataIntoDataBase: Errror::${it.exception?.message}")

                }
            }
        }


    }

    private suspend fun contactEdtTxtFormatIsCorrect(parentData: ParentData): Boolean {
        Log.d(TAG, "contactEdtTxtFormatIsCorrect: parentData:$parentData")
        val contact: String = parentData.contact.trim()
        if (!contact.startsWith("07")) {
              _editParentStatus.send(Resource.error(java.lang.Exception("Contact Must start with 07 !!")))
            return false
        }
        if (contact.length != 10) {
            _editParentStatus.send(Resource.error(java.lang.Exception("Contact Must have 10 characters")))
            return false
        }
        return true
    }

    sealed class Event {
        data class ParentEditSubmitClicked(val parent: ParentData, val photoChanged: Boolean) : Event()
    }

}