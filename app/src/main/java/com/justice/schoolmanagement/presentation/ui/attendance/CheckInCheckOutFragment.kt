package com.justice.schoolmanagement.presentation.ui.attendance

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentCheckInCheckOutBinding
import com.justice.schoolmanagement.presentation.ui.attendance.model.CheckInOut
import com.justice.schoolmanagement.presentation.ui.attendance.model.CurrentPosition
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_check_in_check_out.*


class CheckInCheckOutFragment : Fragment(R.layout.fragment_check_in_check_out), MyLocationListener.LocationListenerCallbacks {
    companion object {
        private const val TAG = "CheckInCheckOutFragment"
    }


    ////////location manager/////////
    private val LOCATION_REFRESH_TIME: Long = 0
    private val LOCATION_PERMISSION = 3

    ///////0 metre///////////////
    private val LOCATION_REFRESH_DISTANCE = 0f

    private var locationManager: LocationManager? = null


    private var mLocationListener: LocationListener? = null


    val RC_LOCATION_PERMISSION = 3

    /////////////////location manager//////////////////


    //////////////fuse location client////
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    //////////////fuse location client////


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


        // checkInBtnClicked()//dummy
        // startLocationUpdates() //dummy
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


        ////////////////////////

        locationCallback = MyLocationCallback { location ->

            val locationAdmin = Location(LocationManager.GPS_PROVIDER) // OR NETWORK_PROVIDER based on the requirement
            locationAdmin.latitude = adminCurrentPosition.latitude
            locationAdmin.longitude = adminCurrentPosition.longitude

            Log.d(TAG, "checkOutBtnClicked: Current postion:  ${location!!.latitude} Long: ${location.longitude} \n Admin Position :" +
                    " ${locationAdmin!!.latitude} Long: ${locationAdmin.longitude}")

            val flag = location.distanceTo(locationAdmin) > adminCurrentPosition.radius

            if (false) {

                Log.d(TAG, "checkOutBtnClicked: you are to far Distance is : ${location.distanceTo(locationAdmin)}  metres expected radius is :${adminCurrentPosition.radius} metres")
                Toasty.error(requireContext(), "Please Move close to the institution Distance is : ${location.distanceTo(locationAdmin)}  metres")
                showProgress(false)
                stopLocationUpdates()
                return@MyLocationCallback
            }

            Log.d(TAG, "checkOutBtnClicked: distance is: ${location.distanceTo(locationAdmin)}  metres")
            stopLocationUpdates()


            FirebaseUtil.getCurrentUser { currentUser ->

                FirebaseUtil.getCurrentDate { date ->
                    val checkOut = mapOf("checkOut" to true, "checkOutTime" to date)

                    documentReferenceCurrentLocation.collection(currentDateFormated).document(FirebaseUtil.getUid()).set(checkOut, SetOptions.merge()).addOnSuccessListener {

                        Toasty.success(requireContext(), "Success You have checked out").show()
                        //  binding.checkOutBtn.isVisible = false

                        showProgress(false)
                    }

                }


            }


        }

        startLocationUpdates();


    }


    private fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates: started location updates")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(0)
        locationRequest.setFastestInterval(0)



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {

                return
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: stopping location updates")
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

    }

    private fun checkInBtnClicked() {
        showProgress(true)


        locationCallback = MyLocationCallback { location ->


            Log.d(TAG, "sendLocation:Lat:  ${location!!.latitude} Long: ${location.longitude}")

            val locationAdmin = Location(LocationManager.GPS_PROVIDER) // OR NETWORK_PROVIDER based on the requirement
            location!!.latitude = adminCurrentPosition.latitude
            location.longitude = adminCurrentPosition.longitude

            val flag = location.distanceTo(locationAdmin) > adminCurrentPosition.radius
            if (false) {
                Log.d(TAG, "sendLocation: change dummy value")
                Log.d(TAG, "checkInBtnClicked: you are to far Distance is : ${location.distanceTo(locationAdmin)}  metres")
                Toasty.error(requireContext(), "Please Move close to the institution Distance is : ${location.distanceTo(locationAdmin)}  metres")


                showProgress(false)
                stopLocationUpdates()
                return@MyLocationCallback
            }

            Log.d(TAG, "checkInBtnClicked: distance is: ${location.distanceTo(locationAdmin)}  metres")

            FirebaseUtil.getCurrentUser { currentUser ->
                val teacherData = currentUser!!.toObject(TeacherData::class.java)


                val checkIn = CheckInOut(teacherData!!.photo, teacherData.fullName, true)

                documentReferenceCurrentLocation.collection(currentDateFormated).document(FirebaseUtil.getUid()).set(checkIn).addOnSuccessListener {

                    Toasty.success(requireContext(), "Success You have checked in").show()
                    binding.checkInBtn.isVisible = false
                    showProgress(false)
                }

            }


        }
        startLocationUpdates()
        ///////////////
        //  setUpLocationManager()
    }

    private fun setUpLocationManager() {
        Log.d(TAG, "setUpLocationManager: ")
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        seeIfGPSisEnabled()
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "setUpLocationManager: permissions available requesting location updates")

            mLocationListener = MyLocationListener(this, locationManager)
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener
            )

        } else {

            Log.d(TAG, "setUpLocationManager: requesting permission")
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION)


        }

    }

    private fun buildAlertMessageNoGps() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first)).setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("No") { dialog, id -> dialog.cancel() }
        builder.show()
    }

    private fun seeIfGPSisEnabled() {
        if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun networkConnectionIsPresent() {
        if (!isOnline()) {
            createNetErrorDialog()
        }
    }

    protected fun isOnline(): Boolean {
        val cm = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return if (netInfo != null && netInfo.isConnected) {
            true
        } else {
            false
        }
    }

    ////////dialog shows when internet connection is not available
    protected fun createNetErrorDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first))
                .setMessage("You need internet connection for this app. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setPositiveButton("Settings"
                ) { dialog, id ->
                    val i = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(i)
                }
                .setNegativeButton("Cancel"
                ) { dialog, id -> findNavController().popBackStack() }
        builder.show()
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == LOCATION_PERMISSION) {
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener)
        }
    }


    override fun onProviderDisabled() {

        if (MyLocationListener.currentLocation == null) {
            return
        }

    }

    private fun checkIfAdminHasSetCurrentPosition() {
        Log.d(TAG, "checkIfAdminHasSetCurrentPosition: ")
        showProgress(true)
        FirebaseUtil.getAdminCurrentLocation {
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

        FirebaseUtil.getCurrentDateFormatted { date ->

            currentDateFormated = date!!
            documentReferenceCurrentLocation.collection(date).document(FirebaseUtil.getUid()).get().addOnSuccessListener {

                if (it.exists()) {
                    userHasAlreadyCheckedIn()

                } else {
                    userNotCheckIn()

                }
                showProgress(false)

            }

        }
    }

    private fun userHasAlreadyCheckedIn() {
        Log.d(TAG, "userHasAlreadyCheckedIn: ")
        binding.checkOutBtn.isVisible = true
    }

    private fun userNotCheckIn() {
        Log.d(TAG, "userNotCheckIn: user not checked in")
        binding.checkInBtn.isVisible = true
    }


    override fun sendLocation(location: Location?) {


    }


    override fun onStop() {
        super.onStop()
        if (locationManager != null) {
            locationManager!!.removeUpdates(mLocationListener)
        }
    }

    ///////////////////////////////////////////////
    fun getLastKnownLocation(onComplete: (Location) -> Unit) {
        Log.d(TAG, "getLastKnownLocation: started getting location")
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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