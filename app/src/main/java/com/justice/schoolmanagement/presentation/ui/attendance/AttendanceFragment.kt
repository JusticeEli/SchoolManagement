package com.justice.schoolmanagement.presentation.ui.attendance

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentAttendanceBinding
import com.justice.schoolmanagement.presentation.ui.attendance.model.AttendanceFragmentAdapter
import com.justice.schoolmanagement.presentation.ui.attendance.model.CheckInOut
import com.justice.schoolmanagement.presentation.ui.chat.util.FirestoreUtil
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty
import java.util.*

class AttendanceFragment : Fragment(R.layout.fragment_attendance) {
    companion object {
        private const val TAG = "AttendanceFragment"
    }

    private val documentReferenceCurrentLocation = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.COLLECTION_ATTENDANCE).document(Constants.DOCUMENT_CURRENT_LOCATION)
    lateinit var currentDateServer: Date
    lateinit var binding: FragmentAttendanceBinding

    lateinit var attendanceFragmentAdapter: AttendanceFragmentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAttendanceBinding.bind(view)
        initProgressBar()
        initRecyclerView()
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.dateBtn.setOnClickListener {


            val myCalendar = Calendar.getInstance()

            val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                //month usually starts from 0
                updateLabel(dayOfMonth, monthOfYear + 1, year)
            }

            binding.dateBtn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    // TODO Auto-generated method stub
                    DatePickerDialog(requireContext(), date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                            myCalendar[Calendar.DAY_OF_MONTH]).show()
                }
            })
        }

        binding.setLocationBtn.setOnClickListener {
            findNavController().navigate(R.id.action_attendanceFragment_to_setLocationFragment)

        }
    }

    private fun updateLabel(dayOfMonth: Int, monthOfYear: Int, year: Int) {
        val data = "$dayOfMonth" + "/" + "${monthOfYear}" + "/" + "$year"
        Log.d(TAG, "updateLabel: ${data}")
//check if we have choosen a future date and reject it if its future date

        val myCalendar = Calendar.getInstance()
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, monthOfYear - 1);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        val choosenDate = myCalendar.time

///checks if we are on same day
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = choosenDate
        cal2.time = currentDateServer

        val sameDay = cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR] &&
                cal1[Calendar.YEAR] == cal2[Calendar.YEAR]

        if (sameDay) {
            //nothing to do hear
        } else if (choosenDate.after(currentDateServer)) {

            myCalendar.get(Calendar.YEAR)

            Toasty.error(requireContext(), "Please Don't Choose  Future date only past  can be choosen").show()
            return
        }

        binding.currentDateTxtView.text = data
        var currentDate=""
        currentDate = data.replace("/", "_")
        currentDate = currentDate.replace("0", "")
        setUpRecyclerView(currentDate)

    }

    private fun setUpRecyclerView(date: String) {
        binding.currentDateTxtView.text = date
        val query: Query = documentReferenceCurrentLocation.collection(date!!)
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<CheckInOut>().setQuery(query, CheckInOut::class.java).setLifecycleOwner(viewLifecycleOwner).build()
        attendanceFragmentAdapter = AttendanceFragmentAdapter(this, firestoreRecyclerOptions)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceFragmentAdapter
        }
        setSwipeListenerForItems()
    }

    private fun initRecyclerView() {

        FirestoreUtil.getCurrentDateFormatted { date ->
            binding.currentDateTxtView.text = date
            val query: Query = documentReferenceCurrentLocation.collection(date!!)
            val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<CheckInOut>().setQuery(query, CheckInOut::class.java).setLifecycleOwner(viewLifecycleOwner).build()
            attendanceFragmentAdapter = AttendanceFragmentAdapter(this, firestoreRecyclerOptions)
            binding.recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = attendanceFragmentAdapter
            }
            setSwipeListenerForItems()

        }


    }

    private fun setSwipeListenerForItems() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                attendanceFragmentAdapter!!.deleteTeacherDataFromDatabase(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(binding.recyclerView)
    }


    /////////////////////PROGRESS_BAR////////////////////////////
    lateinit var dialog: AlertDialog

    fun showProgress(show: Boolean) {

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