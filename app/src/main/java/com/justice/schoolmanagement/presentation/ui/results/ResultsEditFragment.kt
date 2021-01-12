package com.justice.schoolmanagement.presentation.ui.results

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentResultsEditBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import es.dmoral.toasty.Toasty

class ResultsEditFragment : Fragment(R.layout.fragment_results_edit) {

    lateinit var binding: FragmentResultsEditBinding
    private var studentMarks: StudentMarks? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResultsEditBinding.bind(view)
        studentMarks = ApplicationClass.documentSnapshot!!.toObject(StudentMarks::class.java)

        setDefaultValues()
        setOnClickListeners()

    }

    private fun setOnClickListeners() {
        binding.submitBtn.setOnClickListener(View.OnClickListener {
            if (fieldsAreEmpty()) {
                Toasty.error(requireContext(), "Please Fill All Fields", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (marksAreAbove_100()) {
                Toasty.error(requireContext(), "Some Marks Are Not Valid", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            getDataFromEdtTxtAndUpdateInDatabase()
        })
    }

    private fun marksAreAbove_100(): Boolean {
        binding.apply {
            return if (mathEdtTxt.getText().toString().toInt() > 100 || scienceEdtTxt.getText().toString().toInt() > 100 || englishEdtTxt.getText().toString().toInt() > 100 || kiswahiliEdtTxt.getText().toString().toInt() > 100 || sstCreEdtTxt.getText().toString().toInt() > 100) {
                true
            } else false
        }

    }

    private fun getDataFromEdtTxtAndUpdateInDatabase() {
        binding.apply {
            studentMarks!!.math = mathEdtTxt.getText().toString().toInt()
            studentMarks!!.science = scienceEdtTxt.getText().toString().toInt()
            studentMarks!!.english = englishEdtTxt.getText().toString().toInt()
            studentMarks!!.kiswahili = kiswahiliEdtTxt.getText().toString().toInt()
            studentMarks!!.sst_cre = sstCreEdtTxt.getText().toString().toInt()
            studentMarks!!.totalMarks = studentMarks!!.math + studentMarks!!.science + studentMarks!!.english + studentMarks!!.kiswahili + studentMarks!!.sst_cre

        }
        updateInDatabase()
    }

    private fun updateInDatabase() {
        showProgress(true)
        ApplicationClass.documentSnapshot!!.reference.set(studentMarks!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(requireContext(), "Marks Updated", Toast.LENGTH_SHORT).show()
            } else {
                val error = task.exception!!.message
                Toasty.error(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
            }
            showProgress(false)
        }
    }

    private fun fieldsAreEmpty(): Boolean {
        binding.apply {
            return if (mathEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || scienceEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || englishEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || kiswahiliEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty() || sstCreEdtTxt.getText().toString().trim { it <= ' ' }.isEmpty()) {
                true
            } else false
        }

    }

    private fun setDefaultValues() {
        binding.apply {
            nameTxtView.setText(studentMarks!!.fullName)
            mathEdtTxt.setText("" + studentMarks!!.math)
            scienceEdtTxt.setText("" + studentMarks!!.science)
            englishEdtTxt.setText("" + studentMarks!!.english)
            kiswahiliEdtTxt.setText("" + studentMarks!!.kiswahili)
            sstCreEdtTxt.setText("" + studentMarks!!.sst_cre)

        }
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private fun showProgress(show: Boolean) {
        if (show) {
            Toasty.info(requireContext(), "loading...")
        } else {
            Toasty.info(requireContext(), "finished loading")
        }
    }

}