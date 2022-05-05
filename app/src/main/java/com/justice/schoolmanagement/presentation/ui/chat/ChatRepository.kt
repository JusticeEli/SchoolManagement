package com.justice.schoolmanagement.presentation.ui.chat

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.presentation.ui.chat.model.ChatChannel
import com.justice.schoolmanagement.presentation.ui.chat.model.FIELD_TIME
import com.justice.schoolmanagement.presentation.ui.chat.model.Message
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.utils.Constants
import com.justice.schoolmanagement.utils.FirebaseUtil
import com.justice.schoolmanagement.utils.Resource
import com.justice.schoolmanagement.utils.StorageUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

class ChatRepository {

    private val TAG = "ChatRepository"

    fun getChannelId(otherUserId: String) = callbackFlow<Resource<String>> {
        offer(Resource.loading("getting channel id..."))
        FirebaseUtil.currentUserDocRef.collection(Constants.COLLECTION_ENGAGED_CHAT_CHANNELS).document(otherUserId)
                .get().addOnSuccessListener {
                    if (it.exists()) {
                        val channelId = it[Constants.CHANNEL_ID] as String
                        offer(Resource.success(channelId))

                    } else {
                        val currentUserId = FirebaseUtil.getUid()

                        val newChannel = FirebaseUtil.chatChannelsCollectionRef().document()
                        newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                        FirebaseUtil.currentUserDocRef
                                .collection(Constants.COLLECTION_ENGAGED_CHAT_CHANNELS)
                                .document(otherUserId)
                                .set(mapOf(Constants.CHANNEL_ID to newChannel.id))

                        FirebaseUtil.collectionReferenceTeachers().document(otherUserId)
                                .collection(Constants.COLLECTION_ENGAGED_CHAT_CHANNELS)
                                .document(currentUserId)
                                .set(mapOf(Constants.CHANNEL_ID to newChannel.id))

                        offer(Resource.success(newChannel.id))
                    }


                }



        awaitClose { }
    }

    fun getCurrentUser() = callbackFlow<Resource<TeacherData>> {
        offer(Resource.loading("getting current User"))
        Log.d(TAG, "getCurrentUser: path:${FirebaseUtil.currentUserDocRef.path}")
        FirebaseUtil.currentUserDocRef.get().addOnSuccessListener {
            if (it.exists()) {
                val teacher = it.toObject(TeacherData::class.java)!!
                offer(Resource.success(teacher))

            } else {
                offer(Resource.empty())
            }
        }.addOnFailureListener {
            offer(Resource.error(it))

        }


        awaitClose { }
    }

    fun getChats(channelId: String) = callbackFlow<Resource<List<DocumentSnapshot>>> {
        offer(Resource.loading("loading chats"))
        FirebaseUtil.chatChannelsCollectionRef().document(channelId).collection(Constants.COLLECTION_MESSAGES)
                .orderBy(FIELD_TIME)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "ChatMessagesListener error.", firebaseFirestoreException)
                        offer(Resource.error(firebaseFirestoreException))
                    } else {
                        if (querySnapshot!!.isEmpty) {
                            offer(Resource.empty())
                        } else {
                            offer(Resource.success(querySnapshot!!.documents))
                        }
                    }
                }


        awaitClose {

        }
    }

    fun uploadMessageImage(uri: Uri) = callbackFlow<Resource<String>> {
        offer(Resource.loading("uploading message image..."))
        val ref = StorageUtil.currentUserStorageRef.child("messages/${UUID.randomUUID()}")
        val uploadTask = ref.putFile(uri)

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
                offer(Resource.success(downloadUri.toString()))
                Log.d(TAG, "uploadMessageImage: success uploading image")
            } else {
                offer(Resource.error(task.exception))
                val error = task.exception!!.message
                Log.d(TAG, "uploadMessageImage: Error: ${error}")
            }
        }


        awaitClose { }
    }

    fun sendMessage(message: Message, channelId: String) = callbackFlow<Resource<String>> {
        offer(Resource.loading("sending message..."))
        FirebaseUtil.chatChannelsCollectionRef().document(channelId)
                .collection(Constants.COLLECTION_MESSAGES)
                .add(message).addOnSuccessListener {
                    offer(Resource.success(channelId))
                }
                .addOnFailureListener {
                    offer(Resource.error(it))
                }

        awaitClose { }
    }
    fun sendMessage_2(message: Message, channelId: String) = callbackFlow<Resource<String>> {
        offer(Resource.loading("sending message..."))
        FirebaseUtil.chatChannelsCollectionRef().document(channelId)
                .collection(Constants.COLLECTION_MESSAGES)
                .add(message).addOnSuccessListener {
                    offer(Resource.success(channelId))
                }
                .addOnFailureListener {
                    offer(Resource.error(it))
                }

        awaitClose { }
    }
}