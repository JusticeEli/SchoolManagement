package com.justice.schoolmanagement.presentation.utils

object Constants {

    //checks if current user is admin
    public var isAdmin = true
    //Instution
    public var DOCUMENT_CODE = "institution"

    //root
    public val COLLECTION_ROOT = "root/"

    //teacher
    public val COLLECTION_TEACHERS = COLLECTION_ROOT+ DOCUMENT_CODE+"teachers"
    public val COLLECTION_TEACHERS_IMAGES = COLLECTION_ROOT+ DOCUMENT_CODE+"teachers_images"
    public val COLLECTION_TEACHERS_THUMBNAIL_IMAGES = COLLECTION_ROOT+ DOCUMENT_CODE+"teachers_thumbnail_images"

    //student
    public val COLLECTION_STUDENTS = COLLECTION_ROOT+ DOCUMENT_CODE+"students"
    public val COLLECTION_STUDENTS_IMAGES = COLLECTION_ROOT+ DOCUMENT_CODE+"students_images"
    public val COLLECTION_STUDENTS_THUMBNAIL_IMAGES =COLLECTION_ROOT+ DOCUMENT_CODE+ "students_thumbnail_images"
    public val COLLECTION_STUDENTS_MARKS = COLLECTION_ROOT+ DOCUMENT_CODE+"students_marks"

    //parent
   public val COLLECTION_PARENTS =COLLECTION_ROOT+ DOCUMENT_CODE+ "parents"
    public val COLLECTION_PARENTS_IMAGES = COLLECTION_ROOT+ DOCUMENT_CODE+"parents_images"
    public val COLLECTION_PARENTS_THUMBNAIL_IMAGES =COLLECTION_ROOT+ DOCUMENT_CODE+ "parents_thumbnail_images"


    //blog
    public val COLLECTION_BLOGS = COLLECTION_ROOT+ DOCUMENT_CODE+"blogs"
    public val COLLECTION_BLOGS_IMAGES = COLLECTION_ROOT+ DOCUMENT_CODE+"blogs_images"

    //register
    public val COLLECTION_DATE = "dates"



}