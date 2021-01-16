package com.justice.schoolmanagement.presentation.utils

import com.justice.schoolmanagement.presentation.ui.exam.Answer
import java.util.*

object Constants {

    //checks if current user is admin
    public var isAdmin = true

    //Instution
    public var DOCUMENT_CODE = "institution"

    //root
    public val COLLECTION_ROOT = "Institutions/"

    //teacher
    val TEACHERS= "/teachers"
    val TEACHERS_IMAGES= "/teachers_images"
    val TEACHERS_THUMBNAIL_IMAGES=  "/teachers_thumbnail_images"

    public val COLLECTION_TEACHERS1 = COLLECTION_ROOT + DOCUMENT_CODE + TEACHERS
    public val COLLECTION_TEACHERS_IMAGES1 = COLLECTION_ROOT + DOCUMENT_CODE + TEACHERS_IMAGES
    public val COLLECTION_TEACHERS_THUMBNAIL_IMAGES1 = COLLECTION_ROOT + DOCUMENT_CODE +TEACHERS_THUMBNAIL_IMAGES

    //student

    val STUDENTS="/students"
    val STUDENTS_IMAGES="/students_images"
    val STUDENTS_THUMBNAIL_IMAGES="/students_thumbnail_images"
    val STUDENTS_MARKS="/students_marks"

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

    //exam data
    //teachers answers
    var teachersAnswers: ArrayList<Answer> = ArrayList<Answer>()
    var imagePath: String? = null

}