package com.justice.schoolmanagement.presentation.ui.register

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentRegisterBinding
import java.text.SimpleDateFormat
import java.util.*


class RegisterFragment : Fragment(R.layout.fragment_register) {

    companion object {
        private const val TAG = "RegisterFragment"
        lateinit var currentInfo: CurrentInfo

    }


    private var viewPager: ViewPager2? = null
    lateinit var currentDateServer: Date
    lateinit var binding: FragmentRegisterBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)
        setValuesForSpinner()
        getCurrentDateAndInitCurrentInfo()

    }

    private fun setOnClickListeners() {
        val myCalendar = Calendar.getInstance()
        myCalendar.time = currentDateServer
        val date = OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
            myCalendar[Calendar.YEAR] = year
            myCalendar[Calendar.MONTH] = monthOfYear
            myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            updateLabel(dayOfMonth, monthOfYear, year)
        }

        binding.dateBtn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                // TODO Auto-generated method stub
                DatePickerDialog(requireContext(), date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                        myCalendar[Calendar.DAY_OF_MONTH]).show()
            }
        })
    }

    private fun updateLabel(dayOfMonth: Int, monthOfYear: Int, year: Int) {
        val data="$dayOfMonth"+"/"+"${monthOfYear+1}"+"/"+"$year"
        Log.d(TAG, "updateLabel: ${data}")
        binding.currentDateTxtView.text = data


        currentInfo.currentDate=data.replace("/","")

    }


    private fun getCurrentDateAndInitCurrentInfo() {
        currentInfo = CurrentInfo("16", 1.toString(), true)

        FirebaseFirestore.getInstance().collection("dummy").document("date").set(CurrentDate()).addOnSuccessListener {
            Log.d(TAG, "getCurrentDateAndInitCurrentInfo: date sent to database")


            FirebaseFirestore.getInstance().collection("dummy").document("date").get().addOnSuccessListener {

                currentDateServer = it.toObject(CurrentDate::class.java)?.date!!

                setOnClickListeners()
                val date = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(it.toObject(CurrentDate::class.java)?.date)
                binding.currentDateTxtView.text = date


                Log.d(TAG, "getCurrentDateAndInitCurrentInfo: retrieving current date from database ${date}")
                currentInfo.currentDate = date.replace("/", "")
                setUpViewPager()

            }
        }


    }

    fun setUpViewPager() {

        viewPager = binding.viewPager

        viewPager!!.adapter = ViewPagerAdapter(activity) //Attach the adapter with our ViewPagerAdapter passing the host activity

        TabLayoutMediator(binding.tabs, viewPager!!
        ) { tab, position ->
            tab.text = (viewPager!!.adapter as ViewPagerAdapter?)!!.mFragmentNames[position] //Sets tabs names as mentioned in ViewPagerAdapter fragmentNames array, this can be implemented in many different ways.
        }.attach()
    }

    private fun setValuesForSpinner() {
        val classGrade = arrayOf("all", "1", "2", "3", "4", "5", "6", "7", "8")
        val arrayAdapter1: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, classGrade)
        binding.classGradeSpinner.setAdapter(arrayAdapter1)

        binding.classGradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newClass = binding.classGradeSpinner.selectedItem as String

                Log.d(TAG, "onItemSelected: spinner value changed: $newClass")
                currentInfo.currentClass = newClass.trim()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }


}