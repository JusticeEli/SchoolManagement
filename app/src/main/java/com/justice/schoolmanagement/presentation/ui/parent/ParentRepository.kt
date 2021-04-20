package com.justice.schoolmanagement.presentation.ui.parent

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
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


    fun getParents(): Flow<Resource<QuerySnapshot>> = callbackFlow {

        val listenerRegistration = FirebaseUtil.getParents { snapshot, exception ->

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
        val ref = FirebaseUtil.storageReferenceParentsImages().child(photoName)
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
        val ref = FirebaseUtil.storageReferenceParentsImagesThumbnail().child(photoName)
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

        FirebaseUtil.collectionReferenceParents().add(parentData)
                .addOnSuccessListener {
                    Log.d(TAG, "putDataIntoDataBase: success")

                    offer(Resource.success(parentData))

                }.addOnFailureListener {
                    Log.d(TAG, "putDataIntoDataBase: failed")

                    offer(Resource.error(it))

                }


        awaitClose { }
    }

    fun updateDataInDatabase(parentData: ParentData, snapshot: DocumentSnapshot) = callbackFlow<Resource<ParentData>> {

        snapshot.reference.set(parentData)
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

        FirebaseUtil.collectionReferenceParents().document(id).addSnapshotListener { value, error ->
            try {
                if (error != null) {
                    offer(Resource.error(error))

                } else {
                    if (value?.exists()!!) {
                        offer(Resource.success(value!!))
                    } else {
                        offer(Resource.empty())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        awaitClose {

        }
    }

    fun deleteParentPhoto(parentSnapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {

        try {
            val photo = parentSnapshot.getString("photo")
            FirebaseStorage.getInstance().getReferenceFromUrl(photo!!).delete().addOnSuccessListener {
                offer(Resource.success(parentSnapshot))

            }.addOnFailureListener {
                offer(Resource.error(it))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }





        awaitClose { }
    }

    fun deleteParentMetadata(parentSnapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {
        try {
            parentSnapshot.reference.delete().addOnSuccessListener {
                offer(Resource.success(parentSnapshot))
            }.addOnFailureListener {
                offer(Resource.error(it))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


        awaitClose { }
    }

    fun deleteParent(snapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {
        val parent = snapshot.toObject(ParentData::class.java)!!
        FirebaseUtil.firebaseStorage.getReferenceFromUrl(parent.photo).delete().addOnSuccessListener {
            offer(Resource.success(snapshot))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose { }
    }


}