package com.justice.schoolmanagement.presentation.ui.attendance

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentCheckInCheckOutBinding
import com.justice.schoolmanagement.presentation.ui.attendance.model.CheckInOut
import com.justice.schoolmanagement.presentation.ui.attendance.model.CurrentPosition
import com.justice.schoolmanagement.presentation.ui.chat.util.FirestoreUtil
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_check_in_check_out.*

class CheckInCheckOutFragment : Fragment(R.layout.fragment_check_in_check_out) {
    companion object {
        private const val TAG = "CheckInCheckOutFragment"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var adminCurrentPosition: CurrentPosition
    lateinit var currentDateFormated: String
    lateinit var currentTeacherPosition: CurrentPosition
    private val documentReferenceCurrentLocation = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.COLLECTION_ATTENDANCE).document(Constants.DOCUMENT_CURRENT_LOCATION)
    // private val documentReferenceAttendance = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.COLLECTION_ATTENDANCE).document(Constants.DOCUMENT_CURRENT_LOCATION)

    lateinit var binding: FragmentCheckInCheckOutBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCheckInCheckOutBinding.bind(view)
        initProgressBar()
        checkIfAdminHasSetCurrentPosition()
        setOnClickListeners()


    }

    private fun setOnClickListeners() {
        checkInBtn.setOnClickListener {
            checkInBtnClicked()

        }
        checkOutBtn.setOnClickListener {
            checkOutBtnClicked()
        }
    }

    private fun checkOutBtnClicked() {
        showProgress(true)
        getLastKnownLocation { location ->

            val locationAdmin = Location(LocationManager.GPS_PROVIDER) // OR NETWORK_PROVIDER based on the requirement
            location.latitude = adminCurrentPosition.latitude
            location.longitude = adminCurrentPosition.longitude


            if (location.distanceTo(locationAdmin)>adminCurrentPosition.radius){

                Log.d(TAG, "checkInBtnClicked: you are to far Distance is : ${location.distanceTo(locationAdmin)}  metres")
                Toasty.error(requireContext(),"Please Move close to the institution Distance is : ${location.distanceTo(locationAdmin)}  metres")
                return@getLastKnownLocation
            }

            Log.d(TAG, "checkInBtnClicked: distance is: ${location.distanceTo(locationAdmin)}  metres")


            FirestoreUtil.getCurrentUser { currentUser ->

                val checkOut = mapOf("checkOut" to true,"checkOutTime" to null)

                documentReferenceCurrentLocation.collection(currentDateFormated).document(FirestoreUtil.getUid()).set(checkOut, SetOptions.merge()).addOnSuccessListener {

                    Toasty.success(requireContext(), "Success You have checked out").show()
                    binding.checkOutBtn.isVisible=false

                    showProgress(false)
                }

            }


        }   }

    private fun checkInBtnClicked() {
        showProgress(true)
        getLastKnownLocation { location ->


            val locationAdmin = Location(LocationManager.GPS_PROVIDER) // OR NETWORK_PROVIDER based on the requirement
            location.latitude = adminCurrentPosition.latitude
            location.longitude = adminCurrentPosition.longitude


            if (location.distanceTo(locationAdmin)>adminCurrentPosition.radius){

                Log.d(TAG, "checkInBtnClicked: you are to far Distance is : ${location.distanceTo(locationAdmin)}  metres")
                Toasty.error(requireContext(),"Please Move close to the institution Distance is : ${location.distanceTo(locationAdmin)}  metres")
                return@getLastKnownLocation
            }

            Log.d(TAG, "checkInBtnClicked: distance is: ${location.distanceTo(locationAdmin)}  metres")

            FirestoreUtil.getCurrentUser { currentUser ->
                val teacherData = currentUser!!.toObject(TeacherData::class.java)


                val checkIn = CheckInOut(teacherData!!.photo, teacherData.fullName, true)

                documentReferenceCurrentLocation.collection(currentDateFormated).document(FirestoreUtil.getUid()).set(checkIn).addOnSuccessListener {

                    Toasty.success(requireContext(), "Success You have checked in").show()
                    binding.checkInBtn.isVisible=false
                    showProgress(false)
                }

            }


        }
    }

    private fun checkIfAdminHasSetCurrentPosition() {
        Log.d(TAG, "checkIfAdminHasSetCurrentPosition: ")
        showProgress(true)
        FirestoreUtil.getAdminCurrentLocation {
            if (it == null) {
                Log.d(TAG, "checkIfAdminHasSetCurrentPosition: document snapshot is null")
                Toasty.error(requireContext(), "Error: Admin has not set Current position and radius").show()

                showProgress(false)
            } else {
                Log.d(TAG, "checkIfAdminHasSetCurrentPosition: current position available")
                adminCurrentPosition = it.toObject(CurrentPosition::class.java)!!
                check_if_i_have_already_checked_in()

            }


        }
    }

    private fun check_if_i_have_already_checked_in() {
        TODO("Not yet implemented")
        FirestoreUtil.getCurrentDateFormatted { date ->

            currentDateFormated = date!!
            documentReferenceCurrentLocation.collection(date).document(FirestoreUtil.getUid()).get().addOnSuccessListener {

                if (it == null) {
                    userNotCheckIn()
                } else {
                    userCheckIn()
                }
                showProgress(false)

            }

        }
    }

    private fun userCheckIn() {
        Log.d(TAG, "userCheckIn: user  check in")
        binding.checkOutBtn.isVisible = true
    }

    private fun userNotCheckIn() {
        Log.d(TAG, "userNotCheckIn: user not checked in")
        binding.checkInBtn.isVisible = true
    }

    fun getLastKnownLocation(onComplete: (Location) -> Unit) {
        Log.d(TAG, "getLastKnownLocation: started getting location")
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), SetLocationFragment.RC_LOCATION_PERMISSION)


            return
        }

        fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    Log.d(TAG, "getLastKnownLocation: success getting the location")
                    if (location != null) {
                        onComplete(location)
                    } else {
                        Log.d(TAG, "getLastKnownLocation: location is null")
                    }

                }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: ")
        if (requestCode == SetLocationFragment.RC_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {


                Log.d(TAG, "onRequestPermissionsResult: all permissions are granted")

            }

        }


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