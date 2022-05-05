package com.justice.schoolmanagement.presentation.ui.exam

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.DisplayMetrics
import android.util.Log
import com.microsoft.azure.cognitiveservices.vision.computervision.*
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*
import java.io.*
import java.nio.ByteBuffer
import java.util.*


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
/*
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
*/

/*    private fun setDataOnTextview(textview: TextView, firebaseVisionText: FirebaseVisionText) {
        for (textBlock in firebaseVisionText.textBlocks) {
            for (line in textBlock.lines) {
                Log.d(TAG, """
     ${line.text}
     
     """.trimIndent())
            }
        }
        textview.text = firebaseVisionText.text
    }*/


    suspend fun readTextFromImage2(bitmap: Bitmap?, onComplete: (String) -> Unit) {
        Log.d(TAG, "readTextFromImage2: ")

        val subscriptionKey = "358439c19db049cbb02e9150e456e5aa"
        val endpoint = "https://school-management.cognitiveservices.azure.com/"

        // Create an authenticated Computer Vision client.
        val compVisClient = Authenticate(subscriptionKey, endpoint)

        // Read from local file
        ReadFromFile(compVisClient,bitmap!!)


    }


    // </snippet_main>

    // </snippet_main>
    // <snippet_auth>
    fun Authenticate(subscriptionKey: String?, endpoint: String?): ComputerVisionClient {
        return ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint)
    }
    // </snippet_auth>

    // </snippet_auth>
    /**
     * OCR with READ : Performs a Read Operation on a remote image
     * @param client instantiated vision client
     * @param remoteTextImageURL public url from which to perform the read operation against
     */
    private fun ReadFromUrl(client: ComputerVisionClient) {
        println("-----------------------------------------------")
        val remoteTextImageURL = "https://intelligentkioskstore.blob.core.windows.net/visionapi/suggestedphotos/3.png"
        println("Read with URL: $remoteTextImageURL")
        try {
            // Cast Computer Vision to its implementation to expose the required methods
            val vision = client.computerVision() as ComputerVisionImpl

            // Read in remote image and response header
            val responseHeader = vision.readWithServiceResponseAsync(remoteTextImageURL, null, null, null)
                    .toBlocking()
                    .single()
                    .headers()

            // Extract the operation Id from the operationLocation header
            val operationLocation = responseHeader.operationLocation()
            println("Operation Location:$operationLocation")
            getAndPrintReadResult(vision, operationLocation)
        } catch (e: Exception) {
            println(e.message)
            e.printStackTrace()
        }
    }

    /**
     * Convert bitmap to byte array using ByteBuffer.
     */
    fun Bitmap.convertToByteArray(): ByteArray {
        //minimum number of bytes that can be used to store this bitmap's pixels
        val size = this.byteCount

        //allocate new instances which will hold bitmap
        val buffer = ByteBuffer.allocate(size)
        val bytes = ByteArray(size)

        //copy the bitmap's pixels into the specified buffer
        this.copyPixelsToBuffer(buffer)

        //rewinds buffer (buffer position is set to zero and the mark is discarded)
        buffer.rewind()

        //transfer bytes from buffer into the given destination array
        buffer.get(bytes)

        //return bitmap's pixels
        return bytes
    }
    /**
     * OCR with READ : Performs a Read Operation on a local image
     * @param client instantiated vision client
     * @param localFilePath local file path from which to perform the read operation against
     */
    private fun ReadFromFile(client: ComputerVisionClient,bitmap: Bitmap) {
        println("-----------------------------------------------")
        val localFilePath = "src\\main\\resources\\myImage.png"
        println("Read with local file: $localFilePath")
        // </snippet_read_setup>
        // <snippet_read_call>
        try {
            val rawImage = File(localFilePath)
            val localImageBytes: ByteArray = bitmap.convertToByteArray()

            // Cast Computer Vision to its implementation to expose the required methods
            val vision = client.computerVision() as ComputerVisionImpl

            // Read in remote image and response header
            val responseHeader = vision.readInStreamWithServiceResponseAsync(localImageBytes, null, null)
                    .toBlocking()
                    .single()
                    .headers()
            // </snippet_read_call>
            // <snippet_read_response>
            // Extract the operationLocation from the response header
            val operationLocation = responseHeader.operationLocation()
            println("Operation Location:$operationLocation")
            getAndPrintReadResult(vision, operationLocation)
            // </snippet_read_response>
            // <snippet_read_catch>
        } catch (e: Exception) {
            println(e.message)
            e.printStackTrace()
        }
    }
    // </snippet_read_catch>

    // <snippet_opid_extract>
    // </snippet_read_catch>
    // <snippet_opid_extract>
    /**
     * Extracts the OperationId from a Operation-Location returned by the POST Read operation
     * @param operationLocation
     * @return operationId
     */
    private fun extractOperationIdFromOpLocation(operationLocation: String?): String {
        if (operationLocation != null && !operationLocation.isEmpty()) {
            val splits = operationLocation.split("/".toRegex()).toTypedArray()
            if (splits != null && splits.size > 0) {
                return splits[splits.size - 1]
            }
        }
        throw IllegalStateException("Something went wrong: Couldn't extract the operation id from the operation location")
    }
    // </snippet_opid_extract>

    // <snippet_read_result_helper_call>
    // </snippet_opid_extract>
    // <snippet_read_result_helper_call>
    /**
     * Polls for Read result and prints results to console
     * @param vision Computer Vision instance
     * @return operationLocation returned in the POST Read response header
     */
    @Throws(InterruptedException::class)
    private fun getAndPrintReadResult(vision: ComputerVision, operationLocation: String) {
        println("Polling for Read results ...")

        // Extract OperationId from Operation Location
        val operationId = extractOperationIdFromOpLocation(operationLocation)
        var pollForResult = true
        var readResults: ReadOperationResult? = null
        while (pollForResult) {
            // Poll for result every second
            Thread.sleep(1000)
            readResults = vision.getReadResult(UUID.fromString(operationId))

            // The results will no longer be null when the service has finished processing the request.
            if (readResults != null) {
                // Get request status
                val status = readResults.status()
                if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                    pollForResult = false
                }
            }
        }
        // </snippet_read_result_helper_call>

        // <snippet_read_result_helper_print>
        // Print read results, page per page
        for (pageResult in readResults!!.analyzeResult().readResults()) {
            println("")
            println("Printing Read results for page " + pageResult.page())
            val builder = StringBuilder()
            for (line in pageResult.lines()) {
                builder.append(line.text())
                builder.append("\n")
            }
            //println(builder.toString())
            Log.d(TAG, "getAndPrintReadResult: results:${builder.toString()}")
        }
    }
    // </snippet_read_result_helper_print>
}