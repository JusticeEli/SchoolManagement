package com.justice.schoolmanagement.presentation.ui.parent

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.presentation.ui.chat.util.FirestoreUtil
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import com.justice.schoolmanagement.presentation.utils.Constants
import id.zelory.compressor.Compressor
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

class ParentRepository @Inject constructor(private val context: Context) {

    private val TAG = "ParentRepository"


    suspend fun getParents(): Flow<Resource<QuerySnapshot>> = callbackFlow {

        val listenerRegistration = FirestoreUtil.getParents { snapshot, exception ->

            if (exception != null) {
                offer(Resource.error<Nothing>(exception))

                cancel(message = "Error fetching posts",
                        cause = exception)
            } else if (!snapshot!!.isEmpty()) {
                offer(Resource.success(snapshot))
            } else {
                offer(Resource.empty<Nothing>())

            }

        }
        awaitClose { listenerRegistration.remove() }
    }

    fun putPhotoIntoDatabase(photoName: String, uri: Uri) = callbackFlow<Resource<String>> {
        val ref = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS_IMAGES).child(photoName)
        val uploadTask = ref.putFile(uri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnSuccessListener {
            Log.d(TAG, "putPhotoIntoDatabase: success")

            offer(Resource.success(it.toString()))

        }.addOnFailureListener {
            Log.d(TAG, "putPhotoIntoDatabase: failed")
            val error = it!!.message
            offer(Resource.error(java.lang.Exception("Error: $error")))

        }
        awaitClose {

        }
    }

    fun uploadThumbnail(uri: Uri) = callbackFlow<Resource<String>> {


        var thumbnail: Uri? = null
        var compressedImgFile: File? = null
        try {
            compressedImgFile = Compressor(context).setCompressFormat(Bitmap.CompressFormat.JPEG).setMaxHeight(10).setMaxWidth(10).setQuality(40).compressToFile(File(uri.path))
        } catch (e: IOException) {
            e.printStackTrace()
            offer(Resource.error(e))
            return@callbackFlow
        }
        thumbnail = Uri.fromFile(compressedImgFile)

        val photoName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS_THUMBNAIL_IMAGES).child(photoName)
        val uploadTask = ref.putFile(thumbnail)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                offer(Resource.error(task.exception))
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnSuccessListener {
            offer(Resource.success(it.toString()))
        }.addOnFailureListener {
            offer(Resource.error(it))

        }

        awaitClose {

        }
    }

    fun putDataIntoDatabase(parentData: ParentData) = callbackFlow<Resource<ParentData>> {

        FirestoreUtil.collectionReferenceParents.add(parentData)
                .addOnSuccessListener {
                    Log.d(TAG, "putDataIntoDataBase: success")

                    offer(Resource.success(parentData))

                }.addOnFailureListener {
                    Log.d(TAG, "putDataIntoDataBase: failed")

                    offer(Resource.error(it))

                }


        awaitClose { }
    }

    fun getParent(id: String) = callbackFlow<Resource<DocumentSnapshot>> {

        FirestoreUtil.collectionReferenceParents.document(id).addSnapshotListener { value, error ->
            if (error != null) {
                offer(Resource.error(error))

            } else {
                if (value?.exists()!!) {
                    offer(Resource.success(value!!))
                } else {
                    offer(Resource.empty())
                }
            }
        }

        awaitClose {

        }
    }

    fun deleteParentPhoto(parentSnapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {
        val photo = parentSnapshot.getString("photo")
        FirebaseStorage.getInstance().getReferenceFromUrl(photo!!).delete().addOnSuccessListener {
            offer(Resource.success(parentSnapshot))

        }.addOnFailureListener {
            offer(Resource.error(it))
        }




        awaitClose { }
    }

    fun deleteParentMetadata(parentSnapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {

        parentSnapshot.reference.delete().addOnSuccessListener {
            offer(Resource.success(parentSnapshot))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose { }
    }


}