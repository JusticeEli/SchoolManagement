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
import android.os.Bundle
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
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentSetLocationBinding
import com.justice.schoolmanagement.presentation.ui.attendance.model.CurrentPosition
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty


class SetLocationFragment : Fragment(R.layout.fragment_set_location), MyLocationListener.LocationListenerCallbacks {
    ////////////////////////
    ////////0 seconds/////////
    private val LOCATION_REFRESH_TIME: Long = 0
    private val LOCATION_PERMISSION = 3

    ///////0 metre///////////////
    private val LOCATION_REFRESH_DISTANCE = 0f

    private var locationManager: LocationManager? = null


    private var mLocationListener: LocationListener? = null


    ///////////////////////////////////
    companion object {

        val RC_LOCATION_PERMISSION = 3
        private const val TAG = "SetLocationFragment"
    }

    private val documentReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.COLLECTION_ATTENDANCE).document(Constants.DOCUMENT_CURRENT_LOCATION)

    lateinit var binding: FragmentSetLocationBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetLocationBinding.bind(view)
        initProgressBar()

        networkConnectionIsPresent()

        setOnClickListeners()

    }

    private fun setUpLocationManager() {
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        seeIfGPSisEnabled()
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION)
            return
        }
        mLocationListener = MyLocationListener(this, locationManager)
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener)
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

    override fun sendLocation(location: Location?) {
        Log.d(TAG, "sendLocation: ")
        uploadCurrentPosition(location!!)
    }


    override fun onStop() {
        super.onStop()
        if (locationManager != null) {
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

        if (binding.chooseRadiusEditTxt.text.isNullOrBlank()) {
            Toasty.error(requireContext(), "Please Choose Radius").show()
            return
        }

        // getLastKnownLocation()
        // startLocationUpdates()
        showProgress(true)
        setUpLocationManager()


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