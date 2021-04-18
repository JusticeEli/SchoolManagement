package com.justice.schoolmanagement.presentation.ui.teacher

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import id.zelory.compressor.Compressor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject


class TeacherRepository @Inject constructor(private val context: Context) {


    private val TAG = "TeacherRepository"


    fun getTeachers() = callbackFlow<Resource<QuerySnapshot>> {

        val listenerRegistration = FirebaseUtil.getTeachers { snapshot, exception ->

            if (exception != null) {
                offer(Resource.error<Nothing>(exception))

            } else if (!snapshot!!.isEmpty) {
                offer(Resource.success(snapshot))
            } else {
                offer(Resource.empty<Nothing>())

            }

        }
        awaitClose { listenerRegistration.remove() }
    }

    fun putPhotoIntoDatabase(photoName: String, uri: Uri) = callbackFlow<Resource<String>> {

        val id = FirebaseUtil.getUid()
        val ref = FirebaseUtil.storageReferenceTeachersImages.child("$id")
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
            val error = it.message
            offer(Resource.error(Exception("Error: $error")))

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

        val id = FirebaseUtil.getUid()
        val ref = FirebaseUtil.storageReferenceTeachersImagesThumbnail.child("$id")
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

    fun putDataIntoDatabase(teacherData: TeacherData) = callbackFlow<Resource<TeacherData>> {
        val id = FirebaseUtil.getUid()
        FirebaseUtil.collectionReferenceTeachers.document(id)
                .set(teacherData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "putDataIntoDataBase: success")

                    offer(Resource.success(teacherData))

                }.addOnFailureListener {
                    Log.d(TAG, "putDataIntoDataBase: failed")

                    offer(Resource.error(it))

                }


        awaitClose { }
    }

    fun updateDataInDatabase(teacherData: TeacherData, snapshot: DocumentSnapshot) = callbackFlow<Resource<TeacherData>> {
        Log.d(TAG, "updateDataInDatabase: teacherData:$teacherData")
        snapshot.reference
                .set(teacherData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "putDataIntoDataBase: success")

                    offer(Resource.success(teacherData))

                }.addOnFailureListener {
                    Log.d(TAG, "putDataIntoDataBase: failed")

                    offer(Resource.error(it))

                }


        awaitClose { }
    }

    fun getTeacher(id: String) = callbackFlow<Resource<DocumentSnapshot>> {

        FirebaseUtil.collectionReferenceTeachers.document(id).addSnapshotListener { value, error ->
            if (error != null) {
                offer(Resource.error(error))

            } else {
                if (value?.exists()!!) {
                    offer(Resource.success(value))
                } else {
                    offer(Resource.empty())
                }
            }
        }

        awaitClose {

        }
    }

    fun deleteTeacherPhoto(snapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {
        val photo = snapshot.getString("photo")
        FirebaseStorage.getInstance().getReferenceFromUrl(photo!!).delete().addOnSuccessListener {
            offer(Resource.success(snapshot))

        }.addOnFailureListener {
            offer(Resource.error(it))
        }




        awaitClose { }
    }

    fun deleteTeacherMetadata(snapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {

        snapshot.reference.delete().addOnSuccessListener {
            offer(Resource.success(snapshot))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose { }
    }

    fun deleteTeacher(snapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {
        val parent = snapshot.toObject(ParentData::class.java)!!
        FirebaseUtil.firebaseStorage.getReferenceFromUrl(parent.photo).delete().addOnSuccessListener {
            offer(Resource.success(snapshot))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose { }
    }


}