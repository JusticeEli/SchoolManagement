package com.justice.schoolmanagement.presentation.ui.admin

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminBinding.bind(view)
        sharedPreferences = requireContext().getSharedPreferences("shared_pref", MODE_PRIVATE)

        setDefaultValues()
        setOnClickListeners()
    }

    private fun setOnClickListeners() {

        binding.submitBtn.setOnClickListener {
            submitBtnClicked();
        }
    }

    private fun submitBtnClicked() {
        //first check if data is already available in database
        if (TextUtils.isEmpty(institutionCodeEdtTxt.text.toString().trim())) {

            Toasty.error(requireContext(), "Please Fill Institution Code !!").show()
            return
        }
        val institutionCode = institutionCodeEdtTxt.text.toString().trim().replace("/", "")
        firestore.collection(Constants.COLLECTION_ROOT).document(institutionCode).get().addOnSuccessListener {
            if (it.exists()) {
                //u are ordinary teacher
                Constants.DOCUMENT_CODE = institutionCode
                val gson = Gson()
                adminData = AdminData(null, null, institutionCode)
                val stringData = gson.toJson(adminData)
                //save data in share preference
                sharedPreferences.edit().putString(KEY_ADMIN_DATA, stringData).commit()

                findNavController().popBackStack()

            } else {
                ///you are admin first to create this school
                Constants.DOCUMENT_CODE = institutionCode
                fillAdminData()
            }
        }


    }

    private fun fillAdminData() {
        //set visibility of edit text on
        binding.apply {
            nameEdtTxt.isVisible = true
            phoneNumberEdtTxt.isVisible = true
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

    companion object {
        private const val TAG = "AdminFragment"
    }
}