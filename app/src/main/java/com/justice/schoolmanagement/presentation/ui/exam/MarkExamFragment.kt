package com.justice.schoolmanagement.presentation.ui.exam

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.common.reflect.TypeToken
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.gson.Gson
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentMarkExamBinding
import com.justice.schoolmanagement.presentation.utils.Constants
import com.justice.schoolmanagement.presentation.utils.Constants.imagePath
import com.justice.schoolmanagement.presentation.utils.Constants.teachersAnswers
import es.dmoral.toasty.Toasty
import java.lang.reflect.Type
import java.util.*

class MarkExamFragment : Fragment(R.layout.fragment_mark_exam) {

    companion object {
        private const val TAG = "MarkExamFragment"
    }

    //mark
    var rollNumber = String()
    var totalMarksPublic: Int? = null
    var studentAnswers: MutableList<Answer> = mutableListOf()

    //photo
    private val REQ_CAMERA_IMAGE: Int = 6
    lateinit var binding: FragmentMarkExamBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMarkExamBinding.bind(view)
        setOnClickListeners()
        initProgressBar()
    }

    private fun setOnClickListeners() {
        binding.setAnswerBtn.setOnClickListener {

            setAnswerBtnClicked()
        }

        binding.takePhotoBtn.setOnClickListener {
            takePhotoBtnClicked()
        }
        binding.markBtn.setOnClickListener {
            markBtnClicked()
        }
    }

    private fun markBtnClicked() {
        Log.d(TAG, "markBtnClicked: ")
        //getting teachers answers
        fetchAnswerFromSharedPref()

        if (Constants.imagePath == null) {
            Toasty.error(requireContext(), "Please choose an Answer sheet image").show()
            return
        }


        startTheMarkingProcess()


    }

    private fun startTheMarkingProcess() {
        //get upright image
        val bitmap = ImageTextReader.getUprightImage(imagePath)
        //extract text from image using Firebase ML kit on-device or cloud api

        dialog.show()
        ImageTextReader.readTextFromImage(bitmap) { firebaseVisionText ->
            Log.d(TAG, "startTheMarkingProcess: " + firebaseVisionText.text)
            startMarking(firebaseVisionText)
        }
    }

    private fun takePhotoBtnClicked() {
        Log.d(TAG, "takePhotoBtnClicked: ")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQ_CAMERA_IMAGE)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAMERA_IMAGE && resultCode == RESULT_OK) {
            val photo = data?.extras!!["data"] as Bitmap?
            binding.imageView.setImageBitmap(photo)


            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            val tempUri: Uri = getImageUri(requireContext(), photo!!)

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            Log.d(TAG, "onActivityResult: image path is : ${getRealPathFromURI(tempUri)}")
            Constants.imagePath = getRealPathFromURI(tempUri)
            Toasty.success(requireContext(), "Image Choosen").show()
        }


    }

    fun getImageUri(inContext: Context, inImage: Bitmap?): Uri {
        val OutImage = Bitmap.createScaledBitmap(inImage!!, 1000, 1000, true)
        val path = Images.Media.insertImage(inContext.getContentResolver(), OutImage, "Title", null)
        return Uri.parse(path)
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        val contentResolver = requireContext().getContentResolver()
        var path = ""
        if (requireContext().getContentResolver() != null) {
            val cursor: Cursor? = contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    private fun setAnswerBtnClicked() {
        Log.d(TAG, "setAnswerBtnClicked: ")
        findNavController().navigate(R.id.action_markExamFragment_to_setAnswersFragment)
    }

    fun fetchAnswerFromSharedPref() {
        var sharedPreferences: SharedPreferences
        val KEY_ANSWERS = "teacher_answers"
        sharedPreferences = requireContext().getSharedPreferences("shared_pref_teacher_answers", Context.MODE_PRIVATE)

        Log.d(TAG, "fetchAnswerFromSharedPref: ")

        val gson = Gson()
        val json: String? = sharedPreferences.getString(KEY_ANSWERS, null)
        if (json == null) {
            Log.d(TAG, "fetchAnswerFromSharedPref: No default answers found")
            Constants.teachersAnswers.clear()
            for (i in 0..49) {
                val answer = Answer()
                answer.choice = "a"
                answer.number = i
                Constants.teachersAnswers.add(answer)
            }
        } else {
            Log.d(TAG, "fetchAnswerFromSharedPref: default anwers were found")
            val type: Type = object : TypeToken<ArrayList<Answer?>?>() {}.getType()
            Constants.teachersAnswers = gson.fromJson(json, type)
        }


    }

    private fun startMarking(firebaseVisionText: FirebaseVisionText) {
        if (firebaseVisionText.text.isEmpty()) {

            Toasty.error(requireContext(), "Functionality not yet implemented").show()
            return
        }
        var counter = 0
        val sb = StringBuilder()
        Log.d(TAG, "startMarking: started marking")
        for (textBlock in firebaseVisionText.getTextBlocks()) {
            for (line in textBlock.lines) {
                if (counter >= 4 && counter <= 13) {
                    Log.d(TAG, "startMarking: started " + line.text)
                    //we have reached the roll numbers
                    //extract them
                    sb.append(line.text + ":")
                    Log.d(TAG, "startMarking: string builder$sb")
                }
                if (counter == 13) {
                    Log.d(TAG, "startMarking: currently getting roll no")
                    startGettingRollNumber(sb.toString())
                }
                if (counter >= 15 && counter <= 65) {
                    Log.d(TAG, "startMarking: currently getting student marks")
                    startMarkingExam(line.text)
                }
                counter++
            }
        }
        markPapersUsingTeachersAnswers()
    }

    private fun markPapersUsingTeachersAnswers() {

        Constants.teachersAnswers.sortedBy { it.number }
        studentAnswers.sortedBy { it.number }
        Log.d(TAG, "markPapersUsingTeachersAnswers:" + Constants.teachersAnswers)
        Log.d(TAG, "markPapersUsingTeachersAnswers:$studentAnswers")
        var totalMarks = 0
        Log.d(TAG, "markPapersUsingTeachersAnswers: started marking using teachers and student answers")
        for (i in 0..49) {
            if (teachersAnswers.get(i).choice.equals(studentAnswers.get(i).choice)) {
                totalMarks++
                totalMarksPublic = totalMarks
                showResultsToUser(rollNumber, totalMarks)
                Log.d(TAG, "markPapersUsingTeachersAnswers: Answer correct for: $i")
            } else {
                Log.d(TAG, "markPapersUsingTeachersAnswers: Answer Wrong for: $i")
            }
        }
        showProgress(false)
        Log.d(TAG, "markPapersUsingTeachersAnswers: total marks is: $totalMarks")
    }

    private fun startMarkingExam(line: String) {
        Log.d(TAG, "startMarkingExam: line :$line")
        val data = line.split(",", 5) as Array<String>
        val answer = Answer()
        answer.number = Integer.valueOf(data[0].trim { it <= ' ' })
        val choices: MutableList<String> = ArrayList()
        try {
            choices.add(data[1].trim { it <= ' ' }.toLowerCase())
        } catch (e: Exception) {
            Log.d(TAG, "startMarkingExam: Error: " + e.message)
        }
        try {
            choices.add(data[2].toLowerCase().trim { it <= ' ' })
        } catch (e: Exception) {
            Log.d(TAG, "startMarkingExam: Error: " + e.message)
        }
        try {
            choices.add(data[3].toLowerCase().trim { it <= ' ' })
        } catch (e: Exception) {
            Log.d(TAG, "startMarkingExam: Error: " + e.message)
        }
        try {
            choices.add(data[4].toLowerCase().trim { it <= ' ' })
        } catch (e: Exception) {
            Log.d(TAG, "startMarkingExam: Error: " + e.message)
        }
        if (!choices.contains("a")) {
            answer.choice = "a"
        } else if (!choices.contains("b")) {
            answer.choice = "b"
        } else if (!choices.contains("c")) {
            answer.choice = "c"
        } else if (!choices.contains("d")) {
            answer.choice = "d"
        } else {
            answer.choice = null
        }
        Log.d(TAG, "startMarkingExam: answer for question :" + answer.number.toString() + " is : " + answer.choice)
        studentAnswers.add(answer)
    }


    private fun startGettingRollNumber(data: String) {
        val list1: MutableList<String> = ArrayList()
        val list2: MutableList<String> = ArrayList()
        val list3: MutableList<String> = ArrayList()
        val list4: MutableList<String> = ArrayList()
        val lists: MutableList<List<String>> = ArrayList()
        Log.d(TAG, "startGettingRollNumber: started getting roll no$data")
        val rows: Array<String> = data.split(":", 10) as Array<String>
        Log.d(TAG, "startGettingRollNumber: print rows $rows")
        for (row in rows) {
            Log.d(TAG, "startGettingRollNumber: print row$row")
            lists.add(Arrays.asList(*row.split(",", 4) as Array<String>))
        }
        for (list in lists) {
            list1.add(list[0])
            list2.add(list[1])
            list3.add(list[2])
            list4.add(list[3])
        }
        Log.d(TAG, "startGettingRollNumber: list1 :" + list1 + " list2 " + list2
                + " list3 " + list3 + " list4 " + list4)
        fetchNumberFrom(list1)
        fetchNumberFrom(list2)
        fetchNumberFrom(list3)
        fetchNumberFrom(list4)
    }

    private fun fetchNumberFrom(list1: List<String>) {
        for (i in 0..9) {
            try {
                if (i == 5 && list1[i].toUpperCase() == "S") {
                } else {
                    if (list1[i].toInt() != i) {
                        throw Exception("Number At Invalid Index")
                    }
                    val number = list1[i].toInt()
                }
            } catch (e: Exception) {

                ////Exception occurs on the number which is part of the roll number
                Log.d(TAG, "startGettingRollNumber: Error at index " + i + " " + e.message)
                rollNumber += i
                Log.d(TAG, "startGettingRollNumber: Roll number $rollNumber")
                showResultsToUser(rollNumber, totalMarksPublic)
                return
            }
        }
    }

    private fun showResultsToUser(rollNumber: String, totalMarks: Int?) {
        binding.resultsTxtView.setText("Roll number: " + rollNumber + "\nTotal Marks: " + totalMarks)
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    lateinit var dialog: AlertDialog

    private fun showProgress(show: Boolean) {

        if (show) {
            dialog.show()

        } else {
            dialog.dismiss()

        }

    }

    private fun initProgressBar() {

        dialog = setProgressDialog(requireContext(), "Loading..")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
    }

    fun setProgressDialog(context: Context, message: String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    //end progressbar
}

private fun String.split(s: String, i: Int): Any {
    return this.split(s, i)
}
