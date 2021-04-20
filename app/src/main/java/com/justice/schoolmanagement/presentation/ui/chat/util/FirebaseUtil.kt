package com.justice.schoolmanagement.presentation.ui.chat.util

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.justice.schoolmanagement.presentation.ui.chat.model.*
import com.justice.schoolmanagement.presentation.ui.register.CurrentDate
import com.justice.schoolmanagement.presentation.ui.register.CurrentInfo
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import com.justice.schoolmanagement.presentation.utils.Constants.COLLECTION_ENGAGED_CHAT_CHANNELS
import com.justice.schoolmanagement.presentation.utils.Constants.COLLECTION_MESSAGES
import java.text.SimpleDateFormat
import java.util.*


object FirebaseUtil {


    private const val TAG = "FirestoreUtil"


    private val firebaseFirestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val firebaseStorage = FirebaseStorage.getInstance()

    private val currentUserDocRef: DocumentReference
        get() = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS).document(firebaseAuth.currentUser!!.uid)

    // private val chatChannelsCollectionRef = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + "/" + Constants.COLLECTION_CHAT_CHANNELS)
    private fun chatChannelsCollectionRef() = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + "/" + Constants.COLLECTION_CHAT_CHANNELS)


    val collectionReferenceAdmin = firebaseFirestore.collection(Constants.COLLECTION_ROOT)

    //  val collectionReferenceParents = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS)
    fun collectionReferenceParents() = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS)

    //  val collectionReferenceStudents = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS)
    fun collectionReferenceStudents() = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS)

    // val collectionReferenceTeachers = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS)
    fun collectionReferenceTeachers() = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS)

    //  val collectionReferenceStudentsMarks = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_MARKS)
    fun collectionReferenceStudentsMarks() = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_MARKS)

    // val storageReferenceStudentImages = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_IMAGES)
    fun storageReferenceStudentImages() = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_IMAGES)

    //val storageReferenceStudentImagesThumbnail = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_THUMBNAIL_IMAGES)
    fun storageReferenceStudentImagesThumbnail() = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_THUMBNAIL_IMAGES)

    //  val storageReferenceParentsImages = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS_IMAGES)
    fun storageReferenceParentsImages() = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS_IMAGES)

    //  val storageReferenceParentsImagesThumbnail = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS_THUMBNAIL_IMAGES)
    fun storageReferenceParentsImagesThumbnail() = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.PARENTS_THUMBNAIL_IMAGES)

    // val storageReferenceTeachersImages = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS_IMAGES)
    fun storageReferenceTeachersImages() = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS_IMAGES)

    //  val storageReferenceTeachersImagesThumbnail = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS_THUMBNAIL_IMAGES)
    fun storageReferenceTeachersImagesThumbnail() = firebaseStorage.getReference(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS_THUMBNAIL_IMAGES)
    val isUserLoggedIn: Boolean
        get() {
            return firebaseAuth.currentUser != null
        }


    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                        "", null, mutableListOf())
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            } else
                onComplete()
        }
    }

    fun updateCurrentUser(name: String = "", bio: String = "", profilePicturePath: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (bio.isNotBlank()) userFieldMap["bio"] = bio
        if (profilePicturePath != null)
            userFieldMap["profilePicturePath"] = profilePicturePath
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (DocumentSnapshot?) -> Unit) {
        firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS).document(firebaseAuth.currentUser!!.uid).get()
                .addOnSuccessListener {
                    onComplete(it)
                }
    }



    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun getOrCreateChatChannel(otherUserId: String,
                               onComplete: (channelId: String) -> Unit) {
        currentUserDocRef.collection(COLLECTION_ENGAGED_CHAT_CHANNELS)
                .document(otherUserId).get().addOnSuccessListener {
                    if (it.exists()) {
                        onComplete(it["channelId"] as String)
                        return@addOnSuccessListener
                    }

                    val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                    val newChannel = chatChannelsCollectionRef().document()
                    newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                    currentUserDocRef
                            .collection(COLLECTION_ENGAGED_CHAT_CHANNELS)
                            .document(otherUserId)
                            .set(mapOf("channelId" to newChannel.id))

                    firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS).document(otherUserId)
                            .collection(COLLECTION_ENGAGED_CHAT_CHANNELS)
                            .document(currentUserId)
                            .set(mapOf("channelId" to newChannel.id))

                    onComplete(newChannel.id)
                }
    }

    fun addChatMessagesListener(channelId: String, context: Context,
                                onListen: (List<Message>) -> Unit): ListenerRegistration {
        return chatChannelsCollectionRef().document(channelId).collection(COLLECTION_MESSAGES)
                .orderBy("time")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "ChatMessagesListener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Message>()
                    querySnapshot!!.documents.forEach {
                        if (it["type"] == MessageType.TEXT)
                            items.add(it.toObject(TextMessage::class.java)!!)
                        else
                            items.add(it.toObject(ImageMessage::class.java)!!)
                        return@forEach
                    }
                    onListen(items)
                }
    }


    fun getUid():String{
        return FirebaseAuth.getInstance().currentUser!!.uid


    }

    fun getAdminCurrentLocation(onComplete: (DocumentSnapshot?) -> Unit){
     val documentReferenceCurrentLocation = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.COLLECTION_ATTENDANCE).document(Constants.DOCUMENT_CURRENT_LOCATION)
        documentReferenceCurrentLocation.get().addOnSuccessListener {
            if (it.exists()){
                onComplete(it)

            }else{
                onComplete(null)

            }
         }

    }

     fun getCurrentDateFormatted(onComplete: (String?) -> Unit) {
        val currentInfo = CurrentInfo("16", "all", true)

        FirebaseFirestore.getInstance().collection("dummy").document("date").set(CurrentDate()).addOnSuccessListener {


            FirebaseFirestore.getInstance().collection("dummy").document("date").get().addOnSuccessListener {


                val date = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(it.toObject(CurrentDate::class.java)?.date)


                Log.d(TAG, "getCurrentDateAndInitCurrentInfo: retrieving current date from database ${date}")

                //this symbols act weird with database
                currentInfo.currentDate = date.replace("/", "_")
                currentInfo.currentDate = currentInfo.currentDate.replace("0", "")

                onComplete(currentInfo.currentDate)

            }
        }


    }
    fun getCurrentDate(onComplete: (Date?) -> Unit) {
        val currentInfo = CurrentInfo("16", "all", true)

        FirebaseFirestore.getInstance().collection("dummy").document("date").set(CurrentDate()).addOnSuccessListener {


            FirebaseFirestore.getInstance().collection("dummy").document("date").get().addOnSuccessListener {

                onComplete(it.toObject(CurrentDate::class.java)?.date)

            }
        }



    }
    fun sendMessage(message: Message, channelId: String) {
        chatChannelsCollectionRef().document(channelId)
                .collection("messages")
                .add(message)
    }

    //region FCM
    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>?) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject(TeacherData::class.java)!!
            onComplete(user.registrationTokens)
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>) {
        currentUserDocRef.update(mapOf("registrationTokens" to registrationTokens))
    }
    //endregion FCM


    fun getParents(onComplete: (QuerySnapshot?, Exception?) -> Unit): ListenerRegistration {
        return collectionReferenceParents().addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            onComplete(querySnapshot, firebaseFirestoreException)
        }

    }



    fun getStudents(onComplete: (QuerySnapshot?, Exception?) -> Unit): ListenerRegistration {
        return collectionReferenceStudents().addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            onComplete(querySnapshot, firebaseFirestoreException)
        }

    }

    fun getTeachers(onComplete: (QuerySnapshot?, Exception?) -> Unit): ListenerRegistration {
        return collectionReferenceTeachers().addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            onComplete(querySnapshot, firebaseFirestoreException)
        }

    }

    fun getstudentMarks(id: String, onSuccess: (DocumentSnapshot) -> Unit, onFailure: (java.lang.Exception) -> Unit) {
        collectionReferenceStudentsMarks().document(id).get().addOnSuccessListener {
            onSuccess(it)
        }.addOnFailureListener {
            onFailure(it)
        }
    }

    fun getAdminData(institutionCode: String) =
            firebaseFirestore.collection(Constants.COLLECTION_ROOT).document(institutionCode).get()


}