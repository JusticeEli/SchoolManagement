package com.justice.schoolmanagement.presentation.ui.parent

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.presentation.ui.chat.util.FirestoreUtil
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import com.justice.schoolmanagement.presentation.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*

class ParentViewModel @ViewModelInject constructor(repository: ParentRepository, @ApplicationContext private val context: Context) : ViewModel() {
    private val TAG = "ParentViewModel"

    private val _parentChannelEvents = Channel<ParentsFragment.Event>()
    val parentChannelEvents get() = _parentChannelEvents.receiveAsFlow()

    //parents status
    val parentsFetchStatus = flow<Resource<QuerySnapshot>> {
        Log.d(TAG, "started fetching programs: ")
        emit(Resource.loading("started the process"))

        try {
            repository.getParents().collect {
                parentsList.value = it
                emit(it)
            }
        } catch (e: Exception) {
            emit(Resource.error(e))
            Log.e("ERROR:", e.message)
        }
    }

    fun setEvent(event: ParentsFragment.Event) {
        viewModelScope.launch {
            Log.d(TAG, "setEvent:")
            when (event) {
                is ParentsFragment.Event.ParentQuery -> {
                    _parentQueryStatus.send(Resource.loading("started querying"))
                    startQuery(event.query)
                }
                is ParentsFragment.Event.AddParent -> {
                    editParent = false
                    _parentChannelEvents.send(ParentsFragment.Event.AddParent)
                }
                is ParentsFragment.Event.ParentClicked -> {
                    _currentParent.value = event.parentSnapshot
                    _parentChannelEvents.send(ParentsFragment.Event.ParentClicked(event.parentSnapshot))
                }
                is ParentsFragment.Event.ParentEdit -> {
                    _currentParent.value = event.parentSnapshot
                    editParent = true
                    _parentChannelEvents.send(ParentsFragment.Event.ParentEdit(event.parentSnapshot))
                }
                is ParentsFragment.Event.ParentDelete -> {
                    _parentChannelEvents.send(ParentsFragment.Event.ParentDelete(event.parentSnapshot))
                }
                is ParentsFragment.Event.ParentSubmitClicked -> {
                    if (fieldsAreEmpty(event.parent)) {
                        _addParentStatus.send(Resource.empty())
                    } else if (!contactEdtTxtFormatIsCorrect(event.parent)) {
                        _addParentStatus.send(Resource.loading("contact format is incorrect"))
                        /*NO OP*/
                    } else {
                        _addParentStatus.send(Resource.loading("started the uploading parent"))
                        getDataFromEdtTxtAndSaveInDatabase(event.parent)

                    }


                }

            }
        }

    }

    private fun fieldsAreEmpty(parentData: ParentData): Boolean {
        return (parentData.firstName.isBlank()
                || parentData.lastName.isBlank()
                || parentData.email.isBlank()
                || parentData.city.isBlank()
                || parentData.contact.isBlank()
                || parentData.age.isBlank()
                || parentData.jobType.isBlank())

    }

    private suspend fun getDataFromEdtTxtAndSaveInDatabase(parentData: ParentData) {
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



        putPhotoIntoDatabase(parentData)
    }

    private suspend fun putPhotoIntoDatabase(parentData: ParentData) {
        Log.d(TAG, "putPhotoIntoDatabase: parentData:$parentData")
        val photoName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS_IMAGES).child(photoName)
        val uploadTask = ref.putFile(parentData.uri!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                viewModelScope.launch {
                    val downloadUri = task.result
                    parentData.photo = downloadUri.toString()
                    //photo upload success
                    _addParentStatus.send(Resource.loading("full Photo Uploaded"))

                    uploadThumbnail(parentData)

                }
            } else {
                viewModelScope.launch {
                    val error = task.exception!!.message
                    _addParentStatus.send(Resource.error(java.lang.Exception("Error: $error")))

                }

            }

        }

    }

    private suspend fun uploadThumbnail(parentData: ParentData) {
        Log.d(TAG, "uploadThumbnail: parentData:$parentData")
        var thumbnail: Uri? = null
        var compressedImgFile: File? = null
        try {
            compressedImgFile = Compressor(context).setCompressFormat(Bitmap.CompressFormat.JPEG).setMaxHeight(10).setMaxWidth(10).setQuality(40).compressToFile(File(parentData.uri!!.path))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        thumbnail = Uri.fromFile(compressedImgFile)

        //started uploading thumbnail image
        _addParentStatus.send(Resource.loading("uploading thumbnail image started"))
        val photoName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS_THUMBNAIL_IMAGES).child(photoName)
        val uploadTask = ref.putFile(thumbnail)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                parentData.thumbnail = downloadUri.toString()
                viewModelScope.launch {
                    _addParentStatus.send(Resource.loading("Success uploading  thumbnail photo"))
                    putDataIntoDataBase(parentData)

                }
            } else {
                val error = task.exception!!.message
                viewModelScope.launch {
                    _addParentStatus.send(Resource.error(java.lang.Exception("Error: $error")))

                }
            }
        }
    }

    private suspend fun putDataIntoDataBase(parentData: ParentData) {
        Log.d(TAG, "putDataIntoDataBase: parentData:$parentData")
        _addParentStatus.send(Resource.loading("started uploading actual parent data"))


/*
        val parent = ParentData("joh me", "", "", "", "joh me", "", "", "", "", "", "joh me", "")
        FirestoreUtil.collectionReferenceParents.add(parent).addOnSuccessListener {
            Log.d(TAG, "dummyFxn: success")
        }.addOnFailureListener {
            Log.d(TAG, "dummyFxn: failed")
        }
*/


        parentData.uri = null
        FirestoreUtil.collectionReferenceParents.add(parentData)
                .addOnSuccessListener {
                    Log.d(TAG, "putDataIntoDataBase: success")

                    viewModelScope.launch {
                        _addParentStatus.send(Resource.success(ParentData()))

                    }
                }.addOnFailureListener {
                    Log.d(TAG, "putDataIntoDataBase: failed")
                    viewModelScope.launch {
                        val error = it!!.message
                        _addParentStatus.send(Resource.error(java.lang.Exception("Error: $error")))

                    }
                }

        Log.d(TAG, "putDataIntoDataBase: end")
    }

    private suspend fun contactEdtTxtFormatIsCorrect(parentData: ParentData): Boolean {
        Log.d(TAG, "contactEdtTxtFormatIsCorrect: parentData:$parentData")
        val contact: String = parentData.contact.trim()
        if (!contact.startsWith("07")) {
            _addParentStatus.send(Resource.error(java.lang.Exception("Contact Must start with 07 !!")))
            return false
        }
        if (contact.length != 10) {
            _addParentStatus.send(Resource.error(java.lang.Exception("Contact Must have 10 characters")))
            return false
        }
        return true
    }

    val parentsList = MutableStateFlow(Resource.empty<QuerySnapshot>())
    private suspend fun startQuery(query: String) {
        Log.d(TAG, "startQuery: query:$query")
        val filterList = mutableListOf<DocumentSnapshot>()
        Log.d(TAG, "startQuery: size:${parentsList.value.data?.size()}")
        parentsList.value.data?.documents?.forEach { documentSnapshot ->
            val parentData = documentSnapshot.toObject(ParentData::class.java)!!
            if (parentData.fullName.toLowerCase().contains(query.toLowerCase())) {
                filterList.add(documentSnapshot)
            }
        }
        _parentQueryStatus.send(Resource.success(filterList))
    }

    private val _parentQueryStatus = Channel<Resource<List<DocumentSnapshot>>>()
    val parentQueryStatus get() = _parentQueryStatus.receiveAsFlow()

    private val _addParentStatus = Channel<Resource<ParentData>>()
    val addParentStatus get() = _addParentStatus.receiveAsFlow()

    private val _currentParent = MutableLiveData<DocumentSnapshot>()
    val currentParent = _currentParent as LiveData<DocumentSnapshot>
    var editParent = false


}