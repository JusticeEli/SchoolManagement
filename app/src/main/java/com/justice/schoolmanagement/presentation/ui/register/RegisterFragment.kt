package com.justice.schoolmanagement.presentation.ui.register

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.edward.nyansapo.wrappers.Resource
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentRegisterBinding
import com.justice.schoolmanagement.presentation.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    companion object {
        lateinit var currentInfo: CurrentInfo

    }

    private val TAG = "RegisterFragment"

    val firebaseFirestore = FirebaseFirestore.getInstance()
    var listenerRegistration: ListenerRegistration? = null
    private var viewPager: ViewPager2? = null
    lateinit var currentDateServer: Date
    lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()
    @Inject
    lateinit var requestManager: RequestManager
    private lateinit var adapter: RegisterAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)
        setValuesForSpinner()
        setOnClickListeners()
        setUpTabLayout()
        setUpRecyclerView()
        subScribeToObservers()

    }

    private fun setUpRecyclerView() {
        adapter= RegisterAdapter(requestManager,checkBoxClicked = { snapshot,present->onCheckBoxClicked(snapshot,present)})
                .. }

    private fun onCheckBoxClicked(snapshot: DocumentSnapshot, present: Boolean) {
       viewModel.setEvent(Event.CheckBoxClicked(snapshot, present))
    }





    private fun setUpTabLayout() {
        val tab1 = binding.tabs.newTab()
        tab1.text = "All"
        val tab2 = binding.tabs.newTab()
        tab1.text = "Present"
        val tab3 = binding.tabs.newTab()
        tab1.text = "Absent"

        binding.tabs.addTab(tab1)
        binding.tabs.addTab(tab2)
        binding.tabs.addTab(tab3)

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.setEvent(Event.TabSelected(tab!!.position))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getCurrentDate.collect {
                when (it.status) {
                    Resource.Status.LOADING -> {


                    }
                    Resource.Status.SUCCESS -> {
                        receivedCurrentDate(it.data!!)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.registerEvents.collect {
                when (it) {
                    is Event.DateClicked -> {
                        dateClicked()
                    }
                    is Event.SpecificDateChoosen -> {
                        specificDateChoosen(it.date)
                    }
                    is Event.FutureDateChoosen -> {
                        futureDateChoosen()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.fetchDataStatus.collect {
                when(it.status){
                    Resource.Status.LOADING->{

                    }
                    Resource.Status.SUCCESS -> {

                        ada

                    }
                    Resource.Status.ERROR -> {

                    }
                }
            }
        }
    }

    private fun futureDateChoosen() {
        showToastInfo("Please Dont choose future dates")
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun specificDateChoosen(date: Date) {
        updateLabel(date)
    }

    private fun dateClicked() {
        val date = OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub

            val calenderChoosen = Calendar.getInstance()
            calenderChoosen[Calendar.YEAR] = year
            calenderChoosen[Calendar.MONTH] = monthOfYear
            calenderChoosen[Calendar.DAY_OF_MONTH] = dayOfMonth
            viewModel.setEvent(Event.SpecificDateChoosen(calenderChoosen.time))
            updateLabel(dayOfMonth, monthOfYear + 1, year)
        }


        val myCalendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]).show()
    }

    private fun receivedCurrentDate(date: Date) {
        val dateFormatted = SimpleDateFormat("dd/MM/yyyy").format(date)
        Log.d(TAG, "receivedCurrentDate: currentdate:$dateFormatted")
        binding.currentDateTxtView.text = dateFormatted

        val currentInfo=CurrentInfo()
        currentInfo.currentDate = dateFormatted.replace("/", "_")
        currentInfo.currentDate = currentInfo.currentDate.replace("0", "")

        viewModel.setEvent(Event.FetchData(currentInfo))
        setUpViewPager(null)
    }

    private fun setOnClickListeners() {
        binding.dateBtn.setOnClickListener {
            viewModel.setEvent(Event.DateClicked)
        }
    }

    private fun updateLabel(date: Date) {

        binding.currentDateTxtView.text = date.formatDate
        currentInfo.currentDate = date.formatDate.replace("/", "_")
        currentInfo.currentDate = currentInfo.currentDate.replace("0", "")
        setUpViewPager(null)

    }

    private fun updateLabel2(dayOfMonth: Int, monthOfYear: Int, year: Int) {
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



    //checking when to call spinner onItemSelected
    var check = 0
    private fun setValuesForSpinner() {
        val classGrade = requireActivity().resources.getStringArray(R.array.classGradeR)
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

    sealed class Event {
        data class ClassSelected(val classGrade: String) : Event()
        data class TabSelected(val tab: Int) : Event()
        data class SpecificDateChoosen(val date: Date) : Event()
        data class FetchCurrentRegister(val string: String) : Event()
        data class FetchData(val currentInfo: CurrentInfo) : Event()
        data class CheckBoxClicked(val snapshot: DocumentSnapshot,val present: Boolean) : Event()
        object DateClicked : Event()
        object FutureDateChoosen : Event()



}

val Date.formatDate get() = SimpleDateFormat("dd/MM/yyyy").format(this)
