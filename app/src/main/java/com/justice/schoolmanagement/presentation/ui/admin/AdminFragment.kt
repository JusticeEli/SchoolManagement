package com.justice.schoolmanagement.presentation.ui.admin

import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.*
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentAdminBinding
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_admin.*

class AdminFragment : Fragment(R.layout.fragment_admin) {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: FragmentAdminBinding
    private val KEY_ADMIN_DATA = "admin_data"
    lateinit var adminData: AdminData
    val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "AdminFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminBinding.bind(view)
        sharedPreferences = requireContext().getSharedPreferences("shared_pref", MODE_PRIVATE)
        initProgressBar()
        setDefaultValues()
        setOnClickListeners()
    }

    private fun setOnClickListeners() {

        binding.submitBtn.setOnClickListener {
            submitBtnClicked();
        }
    }

    private fun submitBtnClicked() {
        Log.d(TAG, "submitBtnClicked: ")
        //first check if data is already available in database
        if (TextUtils.isEmpty(institutionCodeEdtTxt.text.toString().trim())) {

            Toasty.error(requireContext(), "Please Fill Institution Code !!").show()
            return
        }
        val institutionCode = institutionCodeEdtTxt.text.toString().trim().replace("/", "")
        showProgress(true)
        firestore.collection(Constants.COLLECTION_ROOT).document(institutionCode).get().addOnSuccessListener {
            if (it.exists()) {
                Log.d(TAG, "submitBtnClicked: institution code does not exit setting it")
                //u are ordinary teacher
                Constants.DOCUMENT_CODE = institutionCode
                val gson = Gson()
                adminData = AdminData(null, null, institutionCode)
                val stringData = gson.toJson(adminData)
                //save data in share preference
                sharedPreferences.edit().putString(KEY_ADMIN_DATA, stringData).commit()
                //save institution code//
                Log.d(TAG, "submitBtnClicked: institution code does not exit setting it")
                Constants.DOCUMENT_CODE = institutionCode
                showProgress(false)
                findNavController().popBackStack()

            } else {
                showProgress(false)
                ///you are admin first to create this school
                Constants.DOCUMENT_CODE = institutionCode
                Log.d(TAG, "submitBtnClicked: institution code exists ${Constants.DOCUMENT_CODE }")

                fillAdminData()
            }
        }


    }

    private fun fillAdminData() {

        //set visibility of edit text on
        binding.apply {
            institutionCodeEdtTxt.isEnabled=false
            name.visibility = View.VISIBLE
            phone.visibility = View.VISIBLE
        }
        binding.apply {
            if (TextUtils.isEmpty(nameEdtTxt.text?.trim()) || TextUtils.isEmpty(phoneNumberEdtTxt.text?.trim()) || TextUtils.isEmpty(institutionCodeEdtTxt.text?.trim())) {

                Toasty.error(requireContext(), "Please Fill All Fields !!").show()
                return
            }


            val gson = Gson()
            adminData = AdminData(nameEdtTxt.text.toString().trim(), phoneNumberEdtTxt.text.toString().trim(), institutionCodeEdtTxt.text.toString().trim())
            val stringData = gson.toJson(adminData)
            //save data in share preference
            sharedPreferences.edit().putString(KEY_ADMIN_DATA, stringData).commit()

            saveDataInDatabase()
        }

    }

    private fun saveDataInDatabase() {
        firestore.collection(Constants.COLLECTION_ROOT).document(adminData.institutionCode).set(adminData).addOnSuccessListener {
            Log.d(TAG, "saveDataInDatabase: Admin data saved in database")
            Toasty.success(requireContext(), "Data Saved")
            findNavController().popBackStack()
        }

    }

    private fun setDefaultValues() {

        val stringData = sharedPreferences.getString(KEY_ADMIN_DATA, null)
        if (stringData == null) {
            Log.d(TAG, "setDefaultValues: no default values found...")
            return
        }

        val gson = Gson()
        val adminData = gson.fromJson(stringData, AdminData::class.java)

        binding.nameEdtTxt.setText(adminData.name)
        binding.phoneNumberEdtTxt.setText(adminData.phone)
        binding.institutionCodeEdtTxt.setText(adminData.institutionCode)

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