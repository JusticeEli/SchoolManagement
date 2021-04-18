package com.justice.schoolmanagement.presentation.ui.student

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject


class StudentsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val TAG = "StudentsRepository"

    fun getStudents() = callbackFlow<Resource<QuerySnapshot>> {
        offer(Resource.loading(""))
        val listenerRegistration = FirebaseUtil.getStudents { querySnapshot, exception ->
            if (exception != null) {
                offer(Resource.error(exception))
            } else if (querySnapshot?.isEmpty!!) {
                offer(Resource.empty())
            } else {
                offer(Resource.success(querySnapshot))
            }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    fun deleteStudentPhoto(snapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {

        FirebaseUtil.firebaseStorage.getReferenceFromUrl(snapshot.toObject(StudentData::class.java)!!.photo).delete()
                .addOnSuccessListener {
                    offer(Resource.success(snapshot))
                }.addOnFailureListener {
                    offer(Resource.error(it))
                }

        awaitClose { }
    }


    fun deleteStudentMetaData(snapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {
        snapshot.reference.delete().addOnSuccessListener {
            offer(Resource.success(snapshot))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }
        awaitClose { }
    }

    fun deleteStudentMarks(snapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {
        FirebaseUtil.collectionReferenceStudentsMarks.document(snapshot.id).delete().addOnSuccessListener {
            offer(Resource.success(snapshot))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }
        awaitClose { }
    }

    fun getTeachers() = callbackFlow<Resource<QuerySnapshot>> {

        val listenerRegistration = FirebaseUtil.getTeachers { querySnapshot, exception ->
            if (exception != null) {
                offer(Resource.error(exception))
            } else if (querySnapshot!!.isEmpty) {
                offer(Resource.empty())
            } else {
                offer(Resource.success(querySnapshot))
            }
        }


        awaitClose {
            listenerRegistration.remove()
        }
    }

    fun putPhotoIntoDatabase(photoName: String, uri: Uri) = callbackFlow<Resource<String>> {
        val ref = FirebaseUtil.storageReferenceStudentImages.child(photoName)
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
        val ref = FirebaseUtil.storageReferenceStudentImagesThumbnail.child(photoName)
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

    fun putDataIntoDatabase(studentData: StudentData) = callbackFlow<Resource<DocumentSnapshot>> {

        FirebaseUtil.collectionReferenceStudents.add(studentData)
                .addOnSuccessListener {
                    Log.d(TAG, "putDataIntoDataBase: success")
                    it.get().addOnSuccessListener {
                        offer(Resource.success(it))
                    }

                }.addOnFailureListener {
                    Log.d(TAG, "putDataIntoDataBase: failed")

                    offer(Resource.error(it))

                }


        awaitClose { }
    }

    fun updateDataInDatabase(studentData: StudentData, snapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {

        snapshot.reference.set(studentData)
                .addOnSuccessListener {
                    Log.d(TAG, "putDataIntoDataBase: success")
                    offer(Resource.success(snapshot))

                }.addOnFailureListener {
                    Log.d(TAG, "putDataIntoDataBase: failed")

                    offer(Resource.error(it))

                }


        awaitClose { }
    }

    fun addStudentMarks(snapshot: DocumentSnapshot, studentMarks: StudentMarks) = callbackFlow<Resource<DocumentSnapshot>> {

        FirebaseUtil.collectionReferenceStudentsMarks.document(snapshot.id).set(studentMarks).addOnSuccessListener {
            offer(Resource.success(snapshot))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }

        awaitClose {

        }
    }

    fun getCurrentStudent(id: String) = callbackFlow<Resource<DocumentSnapshot>> {
        Log.d(TAG, "getCurrentStudent:id:$id ")

        val listenerRegistration = FirebaseUtil.collectionReferenceStudents.document(id).addSnapshotListener { value, error ->
            if (error != null) {
                offer(Resource.error(error))
            } else if (!value!!.exists()) {
              //  offer(Resource.error(Exception("Document Does not exit")))
            } else {
                offer(Resource.success(value))
            }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    fun getStudentMarks(id: String) = callbackFlow<Resource<DocumentSnapshot>> {

        FirebaseUtil.getstudentMarks(id, onSuccess = {
            offer(Resource.success(it))
        }) {
            offer(Resource.error(it))
        }

        awaitClose {

        }
    }

    fun updateStudentMarks(snapshot: DocumentSnapshot, studentMarks: StudentMarks)= callbackFlow<Resource<String>> {
        snapshot.reference.set(studentMarks).addOnSuccessListener {
            offer(Resource.success(""))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }


        awaitClose {

        }
    }

}