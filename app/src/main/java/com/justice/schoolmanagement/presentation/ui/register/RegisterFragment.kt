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
import com.google.firebase.firestore.ListenerRegistration
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentRegisterBinding
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*


class RegisterFragment : Fragment(R.layout.fragment_register) {

    companion object {
        private const val TAG = "RegisterFragment"
        lateinit var currentInfo: CurrentInfo

    }

    val firebaseFirestore = FirebaseFirestore.getInstance()
    var listenerRegistration: ListenerRegistration? = null
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
        currentInfo.currentDate = data.replace("/", "_")
        currentInfo.currentDate = currentInfo.currentDate.replace("0", "")
        setUpViewPager(null)

    }


    private fun getCurrentDateAndInitCurrentInfo() {
        currentInfo = CurrentInfo("16", "all", true)

        FirebaseFirestore.getInstance().collection("dummy").document("date").set(CurrentDate()).addOnSuccessListener {
            Log.d(TAG, "getCurrentDateAndInitCurrentInfo: date sent to database")


            FirebaseFirestore.getInstance().collection("dummy").document("date").get().addOnSuccessListener {

                currentDateServer = it.toObject(CurrentDate::class.java)?.date!!

                setOnClickListeners()
                val date = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(it.toObject(CurrentDate::class.java)?.date)
                binding.currentDateTxtView.text = date


                Log.d(TAG, "getCurrentDateAndInitCurrentInfo: retrieving current date from database ${date}")

                //this symbols act weird with database
                currentInfo.currentDate = date.replace("/", "_")
                currentInfo.currentDate = currentInfo.currentDate.replace("0", "")
                setUpViewPager(null)
                //   updateBadgeListener()
            }
        }


    }

    fun setUpViewPager(position:Int?) {
        viewPager = binding.viewPager
        viewPager!!.adapter = ViewPagerAdapter(activity, this) //Attach the adapter with our ViewPagerAdapter passing the host activity


     if (position!=null){
         viewPager!!.postDelayed({ viewPager!!.currentItem = position }, 10)

     }

       TabLayoutMediator(binding.tabs, viewPager!!
        ) { tab, position ->
            tab.text = (viewPager!!.adapter as ViewPagerAdapter?)!!.mFragmentNames[position] //Sets tabs names as mentioned in ViewPagerAdapter fragmentNames array, this can be implemented in many different ways.
        }.attach()

        //  binding.tabs.addOnTabSelectedListener()

    }

    //checking when to call spinner onItemSelected
    var check = 0
    private fun setValuesForSpinner() {
        val classGrade = arrayOf("all", "1", "2", "3", "4", "5", "6", "7", "8")
        val arrayAdapter1: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, classGrade)
        binding.classGradeSpinner.setAdapter(arrayAdapter1)

        binding.classGradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val newClass = binding.classGradeSpinner.selectedItem as String
                currentInfo.currentClass = newClass.trim()

                if (++check > 1) {

                    Log.d(TAG, "onItemSelected: spinner value changed: $newClass")
                    // updateBadgeListener()
                    //  refreshList()
                    setUpViewPager(viewPager?.currentItem)
                    // updateBadgeListener()

                }


            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun updateBadgeListener() {
        Log.d(TAG, "updateBadgeListener: started")
        if (listenerRegistration != null) {
            //    listenerRegistration?.remove()
        }

        listenerRegistration = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.DATE).document(RegisterFragment.currentInfo.currentDate).collection(Constants.STUDENTS).addSnapshotListener { _, _ ->
            Log.d(TAG, "updateBadgeListener: data changed ")
            val adapter = (viewPager?.adapter as ViewPagerAdapter?)

            try {
                if (adapter != null) {
                    val allSize = (adapter.mFragments[0] as AllFragment).registerAdapter.snapshots.size
                    val presentSize = (adapter.mFragments[1] as PresentFragment).registerAdapter.snapshots.size
                    val absentSize = (adapter.mFragments[2] as AbsentFragment).registerAdapter.snapshots.size

                    binding.tabs.getTabAt(0)?.orCreateBadge?.number = allSize
                    binding.tabs.getTabAt(1)?.orCreateBadge?.number = presentSize
                    binding.tabs.getTabAt(2)?.orCreateBadge?.number = absentSize
                    Log.d(TAG, "updateBadgeListener: badges updated")

                }
            } catch (e: Exception) {

            }


        }



        try {

        } catch (e: Exception) {
            Log.e(TAG, "updateBadgeListener: ${e.message}")
        }


    }

    fun refreshList() {
        val adapter = (viewPager?.adapter as ViewPagerAdapter?)

        try {
            if (adapter != null) {


                (adapter.mFragments[0] as AllFragment).setUpFirestore()
                (adapter.mFragments[1] as PresentFragment).setUpFirestore()
                (adapter.mFragments[2] as AbsentFragment).setUpFirestore()


            }
        } catch (e: Exception) {

        }
    }

    fun updateBadge() {
        val adapter = (viewPager?.adapter as ViewPagerAdapter?)

        try {
            if (adapter != null) {
                val allSize = (adapter.mFragments[0] as AllFragment).registerAdapter.snapshots.size
                val presentSize = (adapter.mFragments[1] as PresentFragment).registerAdapter.snapshots.size
                val absentSize = (adapter.mFragments[2] as AbsentFragment).registerAdapter.snapshots.size

                binding.tabs.getTabAt(0)?.orCreateBadge?.number = allSize
                binding.tabs.getTabAt(1)?.orCreateBadge?.number = presentSize
                binding.tabs.getTabAt(2)?.orCreateBadge?.number = absentSize
                Log.d(TAG, "updateBadgeListener: badges updated")

            }
        } catch (e: Exception) {

        }

    }

    fun sendAllFragmentSize(size: Int) {
        binding.tabs.getTabAt(0)?.orCreateBadge?.number = size
    }

    fun sendPresentFragmentSize(size: Int) {
        binding.tabs.getTabAt(1)?.orCreateBadge?.number = size

    }

    fun sendAbsentFragmentSize(size: Int) {
        binding.tabs.getTabAt(2)?.orCreateBadge?.number = size
    }

}