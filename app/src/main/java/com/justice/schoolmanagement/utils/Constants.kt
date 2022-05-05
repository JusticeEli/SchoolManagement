package com.justice.schoolmanagement.utils


object Constants {

    //checks if current user is admin
    public var isAdmin = true

    //Instution
    var DOCUMENT_CODE = "institution"
    var CHANNEL_ID = "channelId"

    //root
    public val COLLECTION_ROOT = "Institutions/"

    //teacher
    val TEACHERS= "/teachers"
    val TEACHERS_IMAGES= "/teachers_images"
    val TEACHERS_THUMBNAIL_IMAGES=  "/teachers_thumbnail_images"

    //teachers chat channels
    val COLLECTION_CHAT_CHANNELS= "chatChannels"
    val COLLECTION_ENGAGED_CHAT_CHANNELS= "engagedChatChannels"
    val COLLECTION_MESSAGES= "messages"
    val COLLECTION_LIKES= "likes"

    public val COLLECTION_TEACHERS1 = COLLECTION_ROOT + DOCUMENT_CODE + TEACHERS
    public val COLLECTION_TEACHERS_IMAGES1 = COLLECTION_ROOT + DOCUMENT_CODE + TEACHERS_IMAGES
    public val COLLECTION_TEACHERS_THUMBNAIL_IMAGES1 = COLLECTION_ROOT + DOCUMENT_CODE + TEACHERS_THUMBNAIL_IMAGES

    //student

    val STUDENTS="/students"
    val STUDENTS_IMAGES="/students_images"
    val STUDENTS_THUMBNAIL_IMAGES="/students_thumbnail_images"
    val STUDENTS_MARKS="/students_marks"


    //student fees
    val COLLECTION_FEES="fees"

    public val COLLECTION_STUDENTS1 = COLLECTION_ROOT + DOCUMENT_CODE + STUDENTS
    public val COLLECTION_STUDENTS_IMAGES1 = COLLECTION_ROOT + DOCUMENT_CODE + STUDENTS_IMAGES
    public val COLLECTION_STUDENTS_THUMBNAIL_IMAGES1 = COLLECTION_ROOT + DOCUMENT_CODE + STUDENTS_THUMBNAIL_IMAGES
    public val COLLECTION_STUDENTS_MARKS1 = COLLECTION_ROOT + DOCUMENT_CODE + STUDENTS_MARKS

    //parent

    val PARENTS="/parents"
    val PARENTS_IMAGES="/parents_images"
    val PARENTS_THUMBNAIL_IMAGES="/parents_thumbnail_images"
    public var COLLECTION_PARENTS1 = COLLECTION_ROOT + DOCUMENT_CODE + PARENTS
    public val COLLECTION_PARENTS_IMAGES1 = COLLECTION_ROOT + DOCUMENT_CODE + PARENTS_IMAGES
    public val COLLECTION_PARENTS_THUMBNAIL_IMAGES1 = COLLECTION_ROOT + DOCUMENT_CODE + PARENTS_THUMBNAIL_IMAGES


    //blog
    val BLOGS="/blogs"
    val BLOGS_IMAGES="/blogs_images"
    public val COLLECTION_BLOGS1 = COLLECTION_ROOT + DOCUMENT_CODE + BLOGS
    public val COLLECTION_BLOGS_IMAGES1 = COLLECTION_ROOT + DOCUMENT_CODE + BLOGS_IMAGES

    //register
    val DATE="/dates"
    public val COLLECTION_DATE1 = COLLECTION_ROOT + DOCUMENT_CODE + DATE


    var imagePath: String? = null


    //attendance
    var COLLECTION_ATTENDANCE="/attendance"
    var DOCUMENT_CURRENT_LOCATION="currentLocation"

}