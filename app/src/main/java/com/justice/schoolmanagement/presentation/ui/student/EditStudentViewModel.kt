package com.justice.schoolmanagement.presentation.ui.student

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class EditStudentViewModel @ViewModelInject constructor(private val repository: StudentsRepository, @Assisted private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val TAG = "EditStudentViewModel"


    val currentStudent = repository.getCurrentStudent(savedStateHandle.get<StudentData>(StudentsFragment.STUDENT_ARGS)!!.id!!)

    fun setEvent(event: EditStudentFragment.Event) {
        viewModelScope.launch {
            when (event) {
                is EditStudentFragment.Event.LoadTeachersNames -> {
                    filterTeachersNames(event.teachersDocumentSnapshot)

                }
                is EditStudentFragment.Event.StudentEditSubmitClicked -> {
                    if (fieldsAreEmpty(event.student)) {
                        _editStudentStatus.send(Resource.empty())
                    } else {
                        _editStudentStatus.send(Resource.loading("started the uploading parent"))
                        trimDataAndSaveIntoDatabase(event.student, event.photoChanged)
                    }
                }
            }
        }

    }

    private val _editStudentStatus = Channel<Resource<StudentData>>()
    val editStudentStatus = _editStudentStatus.receiveAsFlow()

    private val _currentSnapshot = MutableLiveData<DocumentSnapshot>()
    val currentSnapshot = _currentSnapshot as LiveData<DocumentSnapshot>
    fun setCurrentSnapshot(snapshot: DocumentSnapshot) {
        _currentSnapshot.value = snapshot
    }

    private val _editStudentEvents = Channel<EditStudentFragment.Event>()
    val editStudentEvents = _editStudentEvents.receiveAsFlow()
    private suspend fun filterTeachersNames(teachersDocumentSnapshot: List<DocumentSnapshot>) {
        val teacherNames = mutableListOf<String>()
        teachersDocumentSnapshot.forEach {
            val teacher = it.toObject(TeacherData::class.java)!!
            teacherNames.add(teacher.fullName)
        }
        _editStudentEvents.send(EditStudentFragment.Event.SubmitFilteredTeachers(teacherNames))
    }

    val loadTeachersNames = repository.getTeachers()


    private suspend fun trimDataAndSaveIntoDatabase(student: StudentData, photoChange: Boolean) {
        Log.d(TAG, "trimDataAndSaveIntoDatabase: student:$student")
        student.firstName = student.firstName.trim()
        student.lastName = student.lastName.trim()
        student.fullName = "${student.firstName} ${student.firstName}"
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

        if (photoChange) {
            putPhotoIntoDatabase(student)

        } else {
            updateDataInDatabase(student)
        }

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
                    updateDataInDatabase(student)

                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "uploadThumbnail: Error ${it.exception?.message}")

                }
            }
        }


    }

    private suspend fun updateDataInDatabase(student: StudentData) {
        Log.d(TAG, "updateDataInDatabase: student:$student")

        student.uri = null //must be there since firebase does not know how to handle URI object

        repository.updateDataInDatabase(student, currentSnapshot.value!!).collect {
            Log.d(TAG, "updateDataInDatabase: status is: ${it.status}")
            when (it.status) {
                Resource.Status.LOADING -> {

                }
                Resource.Status.SUCCESS -> {
                    getCurrentStudentsMarks(student, it.data!!)

                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "putDataIntoDataBase: Errror::${it.exception?.message}")
                    _editStudentStatus.send(Resource.error(it.exception))

                }
            }
        }


    }

    private suspend fun getCurrentStudentsMarks(student: StudentData, snapshot: DocumentSnapshot) {
        Log.d(TAG, "getCurrentStudentsMarks: student:$student")

        repository.getStudentMarks(snapshot.id).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    updateStudentMarks(it.data!!, student)
                }
                Resource.Status.ERROR -> {
                    _editStudentStatus.send(Resource.error(it.exception))

                }
            }
        }


    }

    private suspend fun updateStudentMarks(snapshot: DocumentSnapshot, student: StudentData) {
        val studentMarks = snapshot.toObject(StudentMarks::class.java)!!
        studentMarks.fullName=student.fullName
        studentMarks.email=student.email
        studentMarks.classGrade=student.classGrade




        repository.updateStudentMarks(snapshot, studentMarks).collect {
            Log.d(TAG, "updateStudentMarks: status:${it.status}")
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    _editStudentStatus.send(Resource.success(student))

                }
                Resource.Status.ERROR -> {
                    _editStudentStatus.send(Resource.error(it.exception))

                }
            }
        }
    }

}