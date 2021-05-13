package com.justice.schoolmanagement.presentation.ui.teacher

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*


class AddTeacherViewModel @ViewModelInject constructor(private val repository: TeacherRepository) : ViewModel() {


    private val TAG = "AddTeacherViewModel"

    //add parent info
    private val _addTeacherStatus = Channel<Resource<TeacherData>>()
    val addTeacherStatus get() = _addTeacherStatus.receiveAsFlow()

    fun setEvent(event: AddTeacherFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is AddTeacherFragment.Event.TeacherAddSubmitClicked -> {
                    if (fieldsAreEmpty(event.teacher)) {
                        _addTeacherStatus.send(Resource.empty())
                    } else if (!contactEdtTxtFormatIsCorrect(event.teacher)) {
                        _addTeacherStatus.send(Resource.error(Exception("contact format is incorrect")))
                        /*NO OP*/
                    } else {
                        _addTeacherStatus.send(Resource.loading("started the uploading parent"))
                        trimDataAndSaveIntoDatabase(event.teacher)
                    }
                }

            }

        }
    }

    private fun fieldsAreEmpty(teacherData: TeacherData): Boolean {
        return (teacherData.firstName.isBlank()
                || teacherData.lastName.isBlank()
                || teacherData.email.isBlank()
                || teacherData.salary.isBlank()
                || teacherData.degree.isBlank()
                || teacherData.city.isBlank()
                || teacherData.contact.isBlank()
                || teacherData.age.isBlank()
                )

    }

    private suspend fun trimDataAndSaveIntoDatabase(teacherData: TeacherData) {
        Log.d(TAG, "trimDataAndSaveIntoDatabase: teacherData:$teacherData")
        teacherData.firstName = teacherData.firstName.trim()
        teacherData.lastName = teacherData.lastName.trim()
        teacherData.fullName = "${teacherData.firstName} ${teacherData.lastName}"
        teacherData.email = teacherData.email.trim()
        teacherData.salary = teacherData.salary.trim()
        teacherData.city = teacherData.city.trim()
        teacherData.degree = teacherData.degree.trim()
        teacherData.age = teacherData.age.trim()
        teacherData.contact = teacherData.contact.trim()



        putPhotoIntoDatabase(teacherData)


    }

    private suspend fun putPhotoIntoDatabase(teacherData: TeacherData) {
        Log.d(TAG, "putPhotoIntoDatabase:started teacherData:$teacherData")
        val photoName = UUID.randomUUID().toString()

        repository.putPhotoIntoDatabase(photoName, teacherData.uri!!).collect {
            Log.d(TAG, "putPhotoIntoDatabase: status of uploading photo: ${it.status.name}")
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    viewModelScope.launch {
                        Log.d(TAG, "putPhotoIntoDatabase: success uploading photo")
                        teacherData.photo = it.data!!

                        uploadThumbnail(teacherData)

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

    private suspend fun uploadThumbnail(teacherData: TeacherData) {
        Log.d(TAG, "uploadThumbnail: teacherData:$teacherData")


        repository.uploadThumbnail(teacherData.uri!!).collect {
            Log.d(TAG, "uploadThumbnail: status of uploading thumbnail: ${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    teacherData.thumbnail = it.data!!
                    putDataIntoDataBase(teacherData)

                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "uploadThumbnail: Error ${it.exception?.message}")

                }
            }
        }


    }

    private suspend fun putDataIntoDataBase(teacherData: TeacherData) {
        Log.d(TAG, "putDataIntoDataBase: teacherData:$teacherData")

        teacherData.uri = null //must be there since firebase does not know how to handle URI object

        repository.putDataIntoDatabase(teacherData).collect {
            Log.d(TAG, "putDataIntoDataBase: status is: ${it.status}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    _addTeacherStatus.send(it)

                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "putDataIntoDataBase: Errror::${it.exception?.message}")

                }
            }
        }


    }

    private suspend fun contactEdtTxtFormatIsCorrect(teacherData: TeacherData): Boolean {
        Log.d(TAG, "contactEdtTxtFormatIsCorrect: teacherData:$teacherData")
        val contact: String = teacherData.contact.trim()
        if (!contact.startsWith("07")) {
            _addTeacherStatus.send(Resource.error(java.lang.Exception("Contact Must start with 07 !!")))
            return false
        }
        if (contact.length != 10) {
            _addTeacherStatus.send(Resource.error(java.lang.Exception("Contact Must have 10 characters")))
            return false
        }
        return true
    }

}
