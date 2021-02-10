package com.justice.schoolmanagement.presentation.ui.fees

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.SetOptions
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentFeesAddEditBinding
import com.justice.schoolmanagement.presentation.ApplicationClass
import com.justice.schoolmanagement.presentation.utils.Constants.COLLECTION_FEES
import es.dmoral.toasty.Toasty

class FeesAddEditFragment : Fragment(R.layout.fragment_fees_add_edit) {
    lateinit var binding: FragmentFeesAddEditBinding
    var updating: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeesAddEditBinding.bind(view)
        initProgressBar()
        checkIfWeAreUpdatingOrAddingFees()
        if (updating) {
            setInitialValues()
        }
        setOnClickListeners()

    }

    private fun setOnClickListeners() {
        binding.saveBtn.setOnClickListener {

            val pay = binding.payedEdtTxt.text.toString().trim()
            if (pay.isNullOrEmpty()) {

                Toasty.error(requireContext(), "Please fill  the payed amount").show()

                return@setOnClickListener
            }

            if (updating) {
                savePayedAmountUpdating(pay.toInt())

            } else {
                savePayedAmountAdding(pay.toInt())

            }


        }
    }

    private fun savePayedAmountAdding(pay: Int) {
        val studentFees = StudentFees(pay)
        showProgress(true)
        ApplicationClass.studentSnapshot!!.reference.collection(COLLECTION_FEES).add(studentFees).addOnSuccessListener {
            Toasty.success(requireContext(), "success adding fees").show()
            showProgress(false)
            findNavController().popBackStack()
        }
    }

    private fun savePayedAmountUpdating(pay: Int) {
        val map = mapOf("payedAmount" to pay)
        showProgress(true)
        ApplicationClass.documentSnapshot!!.reference.set(map, SetOptions.merge()).addOnSuccessListener {
            Toasty.success(requireContext(), "Success adding fees").show()
            showProgress(false)
            findNavController().popBackStack()

        }

    }

    private fun setInitialValues() {
        binding.payedEdtTxt.setText(ApplicationClass.documentSnapshot!!.toObject(StudentFees::class.java)!!.payedAmount.toString())
    }

    private fun checkIfWeAreUpdatingOrAddingFees() {
        updating = ApplicationClass.documentSnapshot != null
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