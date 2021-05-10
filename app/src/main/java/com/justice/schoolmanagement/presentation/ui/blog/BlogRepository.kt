package com.justice.schoolmanagement.presentation.ui.blog

import android.net.Uri
import com.example.edward.nyansapo.wrappers.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.presentation.ui.blog.model.Blog
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

class BlogRepository {
    fun putPhotoInDatabase(uri: Uri) = callbackFlow<Resource<String>> {
        offer(Resource.loading(""))
        val name = UUID.randomUUID().toString()
        val ref = FirebaseUtil.storageReferenceBlogsImages().child(name)
        val uploadTask = ref.putFile(uri!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                offer(Resource.error(task.exception))
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val downLoadLink = downloadUri.toString()

                offer(Resource.success(downLoadLink))
            } else {
                val error = task.exception!!.message
                offer(Resource.error(task.exception))
            }
        }

        awaitClose { }
    }

    fun putBlogIntoDatabase(blog: Blog) = callbackFlow<Resource<Blog>> {

        FirebaseUtil.collectionReferenceBlogs().add(blog).addOnSuccessListener {
            offer(Resource.success(blog))
        }.addOnFailureListener {
            offer(Resource.error(it))
        }
        awaitClose {

        }
    }

    fun getBlogs() = callbackFlow<Resource<List<DocumentSnapshot>>> {

        offer(Resource.loading(""))
        val listenerRegistration = FirebaseUtil.collectionReferenceBlogs().orderBy(FIELD_DATE, Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error!=null){
                offer(Resource.error(error))
            }else if(value!!.isEmpty){
                offer(Resource.empty())
            }else{
                offer(Resource.success(value!!.documents))
            }

        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    fun deleteBlog(snapshot: DocumentSnapshot) = callbackFlow<Resource<DocumentSnapshot>> {
        offer(Resource.loading(""))
        snapshot.reference.delete().addOnSuccessListener {
            offer(Resource.success(snapshot))
        }.addOnFailureListener{
            offer(Resource.error(it))
        }

        awaitClose {  }
    }
}