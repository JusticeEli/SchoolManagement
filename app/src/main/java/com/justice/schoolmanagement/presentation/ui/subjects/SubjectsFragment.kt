package com.justice.schoolmanagement.presentation.ui.subjects

import android.R.layout
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.alldata.AllData
import com.justice.schoolmanagement.databinding.FragmentSubjectsBinding

class SubjectsFragment : Fragment(R.layout.fragment_subjects) {
    private var list: ArrayList<String> = ArrayList()

    lateinit var binding: FragmentSubjectsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubjectsBinding.bind(view)

        setSpinnerValues()
        setOnClickListeners()

    }

    private fun setOnClickListeners() {
        binding.spinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                setDataOnListView(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    private fun setDataOnListView(position: Int) {

        binding.apply {

            when (position) {
                0 -> {
                    val list = getMath()
                    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_list_item_1, list)
                    listView.setAdapter(arrayAdapter)
                }
                1 -> {
                    val list2 = getScience()
                    val arrayAdapter2: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_list_item_1, list2)
                    listView.setAdapter(arrayAdapter2)
                }
                2 -> {
                    val list3 = getEnglish()
                    val arrayAdapter3: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_list_item_1, list3)
                    listView.setAdapter(arrayAdapter3)
                }
                3 -> {
                    val list4 = getKiswahili()
                    val arrayAdapter4: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_list_item_1, list4)
                    listView.setAdapter(arrayAdapter4)
                }
                4 -> {
                    val list5 = getSst_cre()
                    val arrayAdapter5: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_list_item_1, list5)
                    listView.setAdapter(arrayAdapter5)
                }
            }


        }

    }

    private fun getMath(): List<String> {
        list = ArrayList<String>()
        for (teacherData in AllData.teacherDataList) {
            if (teacherData.subject == "Math") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun getScience(): List<String> {
        list = ArrayList<String>()
        for (teacherData in AllData.teacherDataList) {
            if (teacherData.subject == "Science") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun getEnglish(): List<String> {
        list = ArrayList<String>()
        for (teacherData in AllData.teacherDataList) {
            if (teacherData.subject == "English") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun getKiswahili(): List<String> {
        list = ArrayList<String>()
        for (teacherData in AllData.teacherDataList) {
            if (teacherData.subject == "Kiswahili") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun getSst_cre(): List<String> {
        list = ArrayList<String>()
        for (teacherData in AllData.teacherDataList) {
            if (teacherData.subject == "sst_cre") {
                list.add(teacherData.fullName)
            }
        }
        return list
    }

    private fun setSpinnerValues() {
        val subjects = arrayOf("Math", "Science", "English", "Kiswahili", "sst_cre")
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), layout.simple_dropdown_item_1line, subjects)
        binding.spinner.setAdapter(arrayAdapter)
    }


}