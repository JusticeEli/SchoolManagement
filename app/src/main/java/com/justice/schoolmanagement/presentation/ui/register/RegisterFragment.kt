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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentRegisterBinding
import com.justice.schoolmanagement.utils.Resource
import com.justice.schoolmanagement.utils.cleanString
import com.justice.schoolmanagement.utils.formatDate
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {


    private val TAG = "RegisterFragment"


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
        adapter = RegisterAdapter(requestManager, checkBoxClicked = { snapshot, present -> onCheckBoxClicked(snapshot, present) })
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapter
        }

    }

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
                    is Event.CorrectDateChoosen -> {
                        correctDateChoosen(it.currentInfo)
                    }
                    is Event.FutureDateChoosen -> {
                        futureDateChoosen()
                    }
                    is Event.ClassSelected -> {
                        classGradeSelected(it.classGrade)
                    }
                    is Event.TabSelected -> {
                        tabSelected(it.tab)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.fetchDataStatus.collect {
                Log.d(TAG, "subScribeToObservers:fetchDataStatus ${it.status.name}")
                when (it.status) {
                    Resource.Status.LOADING -> {

                    }
                    Resource.Status.SUCCESS -> {
                        adapter.submitList(it.data)

                    }
                    Resource.Status.ERROR -> {

                    }
                }
            }
        }
    }

    private fun tabSelected(curentTab: Int) {
        val classGrade = binding.classGradeSpinner.selectedItem.toString()
        val currentDateString = binding.currentDateTxtView.text.toString().cleanString
        val currentInfo = CurrentInfo(currentDateString, classGrade, curentTab, null)
        viewModel.setEvent(Event.FetchData(currentInfo))
    }

    private fun classGradeSelected(classGrade: String) {
        val currentDateString = binding.currentDateTxtView.text.toString().cleanString
        val currentTab = binding.tabs.selectedTabPosition
        val currentInfo = CurrentInfo(currentDateString, classGrade, currentTab, null)
        viewModel.setEvent(Event.FetchData(currentInfo))

    }

    private fun futureDateChoosen() {
        showToastInfo("Please Dont choose future dates")
    }

    private fun showToastInfo(message: String) {
        Toasty.info(requireContext(), message).show()
    }

    private fun correctDateChoosen(currentInfo: CurrentInfo) {
        updateLabel(currentInfo.dateChoosen!!)
        viewModel.setEvent(Event.FetchData(currentInfo))

    }

    private fun dateClicked() {
        val date = OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub

            val calenderChoosen = Calendar.getInstance()
            calenderChoosen[Calendar.YEAR] = year
            calenderChoosen[Calendar.MONTH] = monthOfYear
            calenderChoosen[Calendar.DAY_OF_MONTH] = dayOfMonth

            val currentClassGrade = binding.classGradeSpinner.selectedItem.toString()
            val currentTab = binding.tabs.selectedTabPosition
            val currentInfo = CurrentInfo(calenderChoosen.time.formatDate.cleanString, currentClassGrade, currentTab, calenderChoosen.time)

            Log.d(TAG, "dateClicked: currentInfo:$currentInfo")
            viewModel.setEvent(Event.CorrectDateChoosen(currentInfo))
        }


        val myCalendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]).show()
    }

    private fun receivedCurrentDate(date: Date) {
        val dateFormatted = SimpleDateFormat("dd/MM/yyyy").format(date)
        Log.d(TAG, "receivedCurrentDate: currentdate:$dateFormatted")
        binding.currentDateTxtView.text = dateFormatted

        val currentInfo = CurrentInfo()
        currentInfo.currentDateString = dateFormatted.replace("/", "_")
        currentInfo.currentDateString = currentInfo.currentDateString.replace("0", "")

        viewModel.setEvent(Event.FetchData(currentInfo))
    }

    private fun setOnClickListeners() {
        binding.dateBtn.setOnClickListener {
            viewModel.setEvent(Event.DateClicked)
        }
    }

    private fun updateLabel(date: Date) {
        binding.currentDateTxtView.text = date.formatDate
    }



    //checking when to call spinner onItemSelected
    var check = 0
    private fun setValuesForSpinner() {
        val classGrade = requireActivity().resources.getStringArray(R.array.classGradeR)
        val arrayAdapter1: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, classGrade)
        binding.classGradeSpinner.setAdapter(arrayAdapter1)

        binding.classGradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if (++check > 1) {
                    val classGrade = binding.classGradeSpinner.selectedItem.toString()
                    Log.d(TAG, "onItemSelected: spinner value changed: ${classGrade}")
                    viewModel.setEvent(Event.ClassSelected(classGrade))

                }


            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

   /* private fun updateBadgeListener() {
        Log.d(TAG, "updateBadgeListener: started")
        if (listenerRegistration != null) {
            //    listenerRegistration?.remove()
        }

        listenerRegistration = firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.DATE).document(RegisterFragment.currentInfo.currentDateString).collection(Constants.STUDENTS).addSnapshotListener { _, _ ->
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
    }*/

    fun sendPresentFragmentSize(size: Int) {
        binding.tabs.getTabAt(1)?.orCreateBadge?.number = size

    }

    fun sendAbsentFragmentSize(size: Int) {
        binding.tabs.getTabAt(2)?.orCreateBadge?.number = size
    }

    sealed class Event {
        data class ClassSelected(val classGrade: String) : Event()
        data class TabSelected(val tab: Int) : Event()
        data class CorrectDateChoosen(val currentInfo: CurrentInfo) : Event()
        data class FetchCurrentRegister(val string: String) : Event()
        data class FetchData(val currentInfo: CurrentInfo) : Event()
        data class CheckBoxClicked(val snapshot: DocumentSnapshot, val present: Boolean) : Event()
        object DateClicked : Event()
        object FutureDateChoosen : Event()


    }

}

