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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentSetLocationBinding
import com.justice.schoolmanagement.presentation.ui.attendance.model.CurrentPosition
import com.justice.schoolmanagement.utils.Constants
import com.justice.schoolmanagement.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetLocationFragment : Fragment(R.layout.fragment_set_location),
    MyLocationListener.LocationListenerCallbacks {

    private val RC_LOCATION_PERMISSION = 3
    private val TAG = "SetLocationFragment"

    //////////////fuse location client////
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    //////////////fuse location client////

    ////////0 seconds/////////
    private val LOCATION_REFRESH_TIME: Long = 0
    private val LOCATION_PERMISSION = 3

    ///////0 metre///////////////
    private val LOCATION_REFRESH_DISTANCE = 0f

    private var locationManager: LocationManager? = null


    private var mLocationListener: LocationListener? = null


    ///////////////////////////////////

    private val documentReference = FirebaseFirestore.getInstance()
        .collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.COLLECTION_ATTENDANCE)
        .document(Constants.DOCUMENT_CURRENT_LOCATION)
    private val viewModel: SetLocationViewModel by viewModels()
    lateinit var binding: FragmentSetLocationBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetLocationBinding.bind(view)
        initProgressBar()
        subScribeToObservers()

        checkIfNetworkConnectionIsPresent()
        setUpLocationManager()
        setOnClickListeners()



    }

    private fun subScribeToObservers() {

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            launch {

                viewModel.setLocationEvents.collect {
                    when (it) {
                        is Event.GoToSettingScreen -> {
                            goToSettingsScreen()
                        }
                        is Event.StartLocationUpdates -> {
                            startLocationUpdates(it.radius)
                        }
                        is Event.StopLocationUpdates -> {
                            stopLocationUpdates()
                        }
                    }
                }

            }


            launch {
                viewModel.setRadiusStatus.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.SUCCESS -> {

                        }
                        Resource.Status.ERROR -> {
                            showToastInfo("Error:${it.exception?.message}")

                        }
                    }
                }
            }
            launch {
                viewModel.uploadCurrentPositionStatus.collect {
                    when (it.status) {
                        Resource.Status.LOADING -> {
                            showProgress(true)

                        }
                        Resource.Status.SUCCESS -> {
                            showProgress(false)

                        }
                        Resource.Status.ERROR -> {
                            showProgress(false)
                            showToastInfo("Error:${it.exception?.message}")

                        }
                    }
                }
            }

        }


    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun goToSettingsScreen() {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)

    }

    private fun setUpLocationManager() {
        Log.d(TAG, "setUpLocationManager: ")
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        seeIfGPSisEnabled()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "setUpLocationManager: permissions available requesting location updates")



        } else {

            Log.d(TAG, "setUpLocationManager: requesting permission")
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION
            )


        }

    }

    private fun buildAlertMessageNoGps() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first))
            .setMessage("Your GPS seems to be disabled, do you want to enable it?")
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

    private fun checkIfNetworkConnectionIsPresent() {
        Log.d(TAG, "checkIfNetworkConnectionIsPresent: ")
        if (!isOnline()) {
            createNetErrorDialog()
        }
    }

    protected fun isOnline(): Boolean {
        val cm =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        val online=netInfo != null && netInfo.isConnected
        Log.d(TAG, "isOnline: online:$online")
        return online
    }

    ////////dialog shows when internet connection is not available
    protected fun createNetErrorDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_first))
            .setMessage("You need internet connection for this app. Please turn on mobile network or Wi-Fi in Settings.")
            .setTitle("Unable to connect")
            .setCancelable(false)
            .setPositiveButton(
                "Settings"
            ) { dialog, id ->
                viewModel.setEvent(Event.GoToSettingScreen)
            }
            .setNegativeButton(
                "Cancel"
            ) { dialog, id -> findNavController().popBackStack() }
        builder.show()
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == LOCATION_PERMISSION) {
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener
            )
        }
    }


    override fun onProviderDisabled() {

        if (MyLocationListener.currentLocation == null) {
            return
        }

    }

    override fun sendLocation(location: Location?) {
        Log.d(TAG, "sendLocation: $location")
        uploadCurrentPosition(location!!)
    }


    override fun onStop() {
        super.onStop()
        if (locationManager != null&&mLocationListener != null) {
            locationManager!!.removeUpdates(mLocationListener)
        }
    }

    private fun setOnClickListeners() {
        binding.apply {
            setBtn.setOnClickListener {
                setBtnClicked()
            }


        }
    }

    private fun setBtnClicked() {
        val radius = binding.chooseRadiusEditTxt.text.toString()
        viewModel.setEvent(Event.SetClicked(radius))

    }


    private fun startLocationUpdates(radius: Int) {
        Log.d(TAG, "startLocationUpdates: started location updates")
        locationCallback = MyLocationCallback { location ->

            viewModel.setEvent(Event.StopLocationUpdates)

            Log.d(TAG, "setBtnClicked: :  ${location!!.latitude} Long: ${location.longitude} ")
            viewModel.setEvent(Event.UploadCurrentPosition(location, radius))

        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(0)
        locationRequest.setFastestInterval(0)



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: ")
        if (::fusedLocationClient.isInitialized){
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun uploadCurrentPosition(location: Location) {

        val radius = binding.chooseRadiusEditTxt.text.toString().trim()

        val currentPosition = CurrentPosition(location.latitude, location.longitude, radius.toInt())

        Log.d(TAG, "uploadCurrentPosition: saving current position in firestore")
        documentReference.set(currentPosition).addOnSuccessListener {
            Log.d(TAG, "uploadCurrentPosition: success saving current position")
            Toasty.success(requireContext(), "Success saving current position").show()
            showProgress(false)
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
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

    sealed class Event {
        object GoToSettingScreen : Event()
        data class SetClicked(val radius: String) : Event()
        data class StartLocationUpdates(val radius: Int) : Event()
        object StopLocationUpdates : Event()
        data class UploadCurrentPosition(val location: Location, val radius: Int) : Event()
    }
}