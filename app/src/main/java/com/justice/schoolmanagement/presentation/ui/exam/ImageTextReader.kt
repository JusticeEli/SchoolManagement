package com.justice.schoolmanagement.presentation.ui.exam

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.DisplayMetrics
import android.util.Log
import android.widget.TextView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.io.IOException

object ImageTextReader {
    private const val TAG = "ImageTextReader"

    //get orientation of an image from exif data of image
    //and perform rotation as required to make it upright
    @JvmStatic
    fun getUprightImage(imgUrl: String?): Bitmap {
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(imgUrl)
        } catch (e: IOException) {
        }
        val orientation = exif!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        var rotation = 0
        when (orientation) {
            3 -> rotation = 180
            6 -> rotation = 90
            8 -> rotation = 270
        }
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        var bitmap = BitmapFactory.decodeFile(imgUrl)
        //rotate image
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width,
                bitmap.height, matrix, true)
        return bitmap
    }

    //resize image to device width
    fun resizeImage(bitmap: Bitmap?, ctx: Context): Bitmap {
        val displayMetrics = DisplayMetrics()
        (ctx as Activity).windowManager
                .defaultDisplay
                .getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        return Bitmap.createScaledBitmap(bitmap!!, width, width, true)
    }

    //read text from image using Firebase ML kit api
    //on-device api
 /*   fun readTextFromImage(bitmap: Bitmap?, textView: TextView) {
        val image = FirebaseVisionImage.fromBitmap(bitmap!!)
        val firebaseVisionTextReturn = arrayOfNulls<FirebaseVisionText>(1)
        val textRecognizer = FirebaseVision.getInstance()
                .onDeviceTextRecognizer
        Log.d(TAG, "readTextFromImage: started reading text from image")
        val result = textRecognizer.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    Log.d(TAG, "onSuccess:  success reading text")
                    setDataOnTextview(textView, firebaseVisionText)
                }
                .addOnFailureListener { e -> Log.d(TAG, "onFailure: failed reading text" + e.message) }
        Log.d(TAG, "readTextFromImage: returned firebase vision text")
    }
*/
    //read text from image using Firebase ML kit api
    //on-device api
    fun readTextFromImage(bitmap: Bitmap?, returnFirebaseVisionText: (FirebaseVisionText) -> Unit) {
        val image = FirebaseVisionImage.fromBitmap(bitmap!!)
        val firebaseVisionTextReturn = arrayOfNulls<FirebaseVisionText>(1)
        val textRecognizer = FirebaseVision.getInstance()
                .onDeviceTextRecognizer
        Log.d(TAG, "readTextFromImage: started reading text from image")
        val result = textRecognizer.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    Log.d(TAG, "readTextFromImage: Text should be printed here" + firebaseVisionText.text)
                    returnFirebaseVisionText(firebaseVisionText)
                }.addOnFailureListener { e -> Log.d(TAG, "onFailure: failed reading text" + e.message) }
        Log.d(TAG, "readTextFromImage: returned firebase vision text")
    }

    private fun setDataOnTextview(textview: TextView, firebaseVisionText: FirebaseVisionText) {
        for (textBlock in firebaseVisionText.textBlocks) {
            for (line in textBlock.lines) {
                Log.d(TAG, """
     ${line.text}
     
     """.trimIndent())
            }
        }
        textview.text = firebaseVisionText.text
    }


}