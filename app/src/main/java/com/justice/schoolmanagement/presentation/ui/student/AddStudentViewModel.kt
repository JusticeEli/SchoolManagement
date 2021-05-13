package com.justice.schoolmanagement.presentation.ui.student

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class AddStudentViewModel @ViewModelInject constructor(private val repository: StudentsRepository) : ViewModel() {

    private val TAG = "AddStudentViewModel"


    private val _addStudentEvents = Channel<AddStudentFragment.Event>()
    val addStudentEvents = _addStudentEvents.receiveAsFlow()
    private val _addStudentStatus = Channel<Resource<StudentData>>()
    val addStudentStatus = _addStudentStatus.receiveAsFlow()

    fun setEvent(event: AddStudentFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is AddStudentFragment.Event.LoadTeachersNames -> {
                    filterTeachersNames(event.teachersDocumentSnapshot)

                }
                is AddStudentFragment.Event.StudentAddSubmitClicked -> {
                    if (fieldsAreEmpty(event.student)) {
                        _addStudentStatus.send(Resource.empty())
                    } else {
                        _addStudentStatus.send(Resource.loading("started the uploading parent"))
                        trimDataAndSaveIntoDatabase(event.student)
                    }

                }
            }
        }

    }

    private suspend fun trimDataAndSaveIntoDatabase(student: StudentData) {
        Log.d(TAG, "trimDataAndSaveIntoDatabase: student:$student")
        student.firstName = student.firstName.trim()
        student.lastName = student.lastName.trim()
        student.fullName = "${student.firstName} ${student.lastName}"
        student.classGrade = student.classGrade
        student.nationality = student.nationality.trim()
        student.religion = student.religion.trim()
        student.email = student.email.trim()
        student.parentName = student.parentName.trim()
        student.dateOfBirth = student.dateOfBirth.trim()
        student.dateOfArrival = student.dateOfArrival.trim()
        student.age = student.age
        student.gender = student.gender.trim()
        student.classTeacherName = student.classTeacherName.trim()
        student.city = student.city.trim()

        putPhotoIntoDatabase(student)
    }

    private fun fieldsAreEmpty(student: StudentData): Boolean {
        Log.d(TAG, "fieldsAreEmpty: checking if fields are empty")
        return (student.firstName.isBlank()
                || student.lastName.isBlank()
                || student.email.isBlank()
                || student.parentName.isBlank()
                || student.dateOfBirth.isBlank()
                || student.dateOfArrival.isBlank()
                || student.age.isBlank()
                || student.city.isBlank()
                )

    }

    private suspend fun putPhotoIntoDatabase(student: StudentData) {
        Log.d(TAG, "putPhotoIntoDatabase:started StudentData:$student")
        val photoName = UUID.randomUUID().toString()

        repository.putPhotoIntoDatabase(photoName, student.uri!!).collect {
            Log.d(TAG, "putPhotoIntoDatabase: status of uploading photo: ${it.status.name}")
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    viewModelScope.launch {
                        Log.d(TAG, "putPhotoIntoDatabase: success uploading photo")
                        student.photo = it.data!!

                        uploadThumbnail(student)

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

    private suspend fun uploadThumbnail(student: StudentData) {
        Log.d(TAG, "uploadThumbnail: student:$student")


        repository.uploadThumbnail(student.uri!!).collect {
            Log.d(TAG, "uploadThumbnail: status of uploading thumbnail: ${it.status.name}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    student.thumbnail = it.data!!
                    putDataIntoDataBase(student)

                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "uploadThumbnail: Error ${it.exception?.message}")

                }
            }
        }


    }

    private suspend fun putDataIntoDataBase(student: StudentData) {
        Log.d(TAG, "putDataIntoDataBase: student:$student")

        student.uri = null //must be there since firebase does not know how to handle URI object

        repository.putDataIntoDatabase(student).collect {
            Log.d(TAG, "putDataIntoDataBase: status is: ${it.status}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    addStudentMarks(it.data!!)

                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "putDataIntoDataBase: Errror::${it.exception?.message}")
                    _addStudentStatus.send(Resource.error(it.exception))

                }
            }
        }


    }

    private suspend fun addStudentMarks(snapshot: DocumentSnapshot) {
        val student = snapshot.toObject(StudentData::class.java)!!
        Log.d(TAG, "addStudentMarks: student:$student")
        val studentMarks = StudentMarks()
        studentMarks.fullName=student.fullName
        studentMarks.email=student.email
        studentMarks.classGrade=student.classGrade


        repository.addStudentMarks(snapshot, studentMarks).collect {
            Log.d(TAG, "addStudentMarks: status:${it.status}")
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    val student=it.data!!.toObject(StudentData::class.java)!!
                   _addStudentStatus.send(Resource.success(student))

                }
                Resource.Status.ERROR -> {
                    _addStudentStatus.send(Resource.error(it.exception))

                }
            }
        }


    }

    private suspend fun filterTeachersNames(teachersDocumentSnapshot: List<DocumentSnapshot>) {
        val teacherNames = mutableListOf<String>()
        teachersDocumentSnapshot.forEach {
            val teacher = it.toObject(TeacherData::class.java)!!
            teacherNames.add(teacher.fullName)
        }
        _addStudentEvents.send(AddStudentFragment.Event.SubmitFilteredTeachers(teacherNames))
    }

    val loadTeachersNames = repository.getTeachers()
}