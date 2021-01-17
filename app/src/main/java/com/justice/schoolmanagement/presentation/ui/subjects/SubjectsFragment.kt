package com.justice.schoolmanagement.presentation.ui.subjects

import android.R.layout
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.alldata.AllData
import com.justice.schoolmanagement.databinding.FragmentSubjectsBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import java.util.*

class SubjectsFragment : Fragment(R.layout.fragment_subjects) {
    private var list: ArrayList<String> = ArrayList()

    lateinit var binding: FragmentSubjectsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubjectsBinding.bind(view)
        initProgressBar()
        loadTeacherNames()

    }

    fun loadTeacherNames() {
        showProgress(true)
        ApplicationClass.teacherNames.clear()
        AllData.teacherDataList.clear()
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS).get().addOnCompleteListener { task ->
            //   Toast.makeText(this@ApplicationClass, "Loading Teachers name: ", Toast.LENGTH_SHORT).show()
            if (task.isSuccessful) {
                ApplicationClass.teacherNames.clear()
                for (documentSnapshot in task.result!!) {
                    ApplicationClass.teacherNames.add(documentSnapshot.toObject(TeacherData::class.java).fullName)
                    AllData.teacherDataList.add(documentSnapshot.toObject(TeacherData::class.java))
                }
                setSpinnerValues()
                setOnClickListeners()

            } else {
                Toast.makeText(requireContext(), "Error: " + task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }

    private fun setOnClickListeners() {
        binding.spinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                setDataOnListView(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    private fun setDataOnListView(position: Int) {

        binding.apply {

            when (position) {
                0 -> {
                    val list = getMath()
                    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_list_item_1, list)
                    listView.setAdapter(arrayAdapter)
                }
                1 -> {
                    val list2 = getScience()
                    val arrayAdapter2: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_list_item_1, list2)
                    listView.setAdapter(arrayAdapter2)
                }
                2 -> {
                    val list3 = getEnglish()
                    val arrayAdapter3: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_list_item_1, list3)
                    listView.setAdapter(arrayAdapter3)
                }
                3 -> {
                    val list4 = getKiswahili()
                    val arrayAdapter4: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_list_item_1, list4)
                    listView.setAdapter(arrayAdapter4)
                }
                4 -> {
                    val list5 = getSst_cre()
                    val arrayAdapter5: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_list_item_1, list5)
                    listView.setAdapter(arrayAdapter5)
                }
            }


        }

    }

    private fun getMath(): List<String> {
        list = ArrayList<String>()
        for (teacherData in AllData.teacherDataList) {
            if (teacherData.subject == "Math") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun getScience(): List<String> {
        list = ArrayList<String>()
        for (teacherData in AllData.teacherDataList) {
            if (teacherData.subject == "Science") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun getEnglish(): List<String> {
        list = ArrayList<String>()
        for (teacherData in AllData.teacherDataList) {
            if (teacherData.subject == "English") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun getKiswahili(): List<String> {
        list = ArrayList<String>()
        for (teacherData in AllData.teacherDataList) {
            if (teacherData.subject == "Kiswahili") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun getSst_cre(): List<String> {
        list = ArrayList<String>()
        for (teacherData in AllData.teacherDataList) {
            if (teacherData.subject == "sst_cre") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun setSpinnerValues() {
        val subjects = arrayOf("Math", "Science", "English", "Kiswahili", "sst_cre")
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_dropdown_item_1line, subjects)
        binding.spinner.setAdapter(arrayAdapter)
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