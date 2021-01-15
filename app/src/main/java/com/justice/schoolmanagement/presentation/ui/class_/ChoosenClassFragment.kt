package com.justice.schoolmanagement.presentation.ui.class_

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.alldata.AllData
import com.justice.schoolmanagement.databinding.FragmentChoosenClassBinding
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_choosen_class.*

class ChoosenClassFragment : Fragment(R.layout.fragment_choosen_class) {
    private var class_ = 0

    private var choosenClassActivityResultsRecyclerAdapter: ChoosenClassActivityResultsRecyclerAdapter? = null
    private var choosenClassActivityStudentsRecyclerAdapter: ChoosenClassActivityStudentsRecyclerAdapter? = null


    private val choosenClassStudentDataList = mutableListOf<StudentData>()
    private val choosenClassStudentMarkList = mutableListOf<StudentMarks>()
    private var counter = 1

    lateinit var binding: FragmentChoosenClassBinding
    val navArgs: ChoosenClassFragmentArgs by navArgs()

    lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChoosenClassBinding.bind(view)
        navController = findNavController()
        class_ = navArgs.classNumber



        setUpRecyclerView()
        setDefaultValues()
        setOnClickListeners()

    }

    private fun setDefaultValues() {
        when (class_) {
            1 -> {
                headerTxtView.setText("Class 1")
                getClass_1_data()
            }
            2 -> {
                headerTxtView.setText("Class 2")
                getClass_2_data()
            }
            3 -> {
                headerTxtView.setText("Class 3")
                getClass_3_data()
            }
            4 -> {
                headerTxtView.setText("Class 4")
                getClass_4_data()
            }
            5 -> {
                headerTxtView.setText("Class 5")
                getClass_5_data()
            }
            6 -> {
                headerTxtView.setText("Class 6")
                getClass_6_data()
            }
            7 -> {
                headerTxtView.setText("Class 7")
                getClass_7_data()
            }
            8 -> {
                headerTxtView.setText("Class 8")
                getClass_8_data()
            }
            else -> {
            }
        }
    }

    private fun getClass_1_data() {
        for (studentData in AllData.studentDataList) {
            if (studentData.classGrade == 1) {
                choosenClassStudentDataList.add(studentData)
            }
        }
        for (studentMarks in AllData.studentMarksList) {
            if (studentMarks.classGrade == 1) {

                choosenClassStudentMarkList.add(studentMarks)
            }
        }
    }

    private fun getClass_2_data() {
        for (studentData in AllData.studentDataList) {
            if (studentData.classGrade == 2) {
                choosenClassStudentDataList.add(studentData)
            }
        }
        for (studentMarks in AllData.studentMarksList) {
            if (studentMarks.classGrade == 2) {
                choosenClassStudentMarkList.add(studentMarks)
            }
        }
    }

    private fun getClass_3_data() {
        for (studentData in AllData.studentDataList) {
            if (studentData.classGrade == 3) {
                choosenClassStudentDataList.add(studentData)
            }
        }
        for (studentMarks in AllData.studentMarksList) {
            if (studentMarks.classGrade == 3) {
                choosenClassStudentMarkList.add(studentMarks)
            }
        }
    }

    private fun getClass_4_data() {
        for (studentData in AllData.studentDataList) {
            if (studentData.classGrade == 4) {
                choosenClassStudentDataList.add(studentData)
            }
        }
        for (studentMarks in AllData.studentMarksList) {
            if (studentMarks.classGrade == 4) {
                choosenClassStudentMarkList.add(studentMarks)
            }
        }
    }

    private fun getClass_5_data() {
        for (studentData in AllData.studentDataList) {
            if (studentData.classGrade == 5) {
                choosenClassStudentDataList.add(studentData)
            }
        }
        for (studentMarks in AllData.studentMarksList) {
            if (studentMarks.classGrade == 5) {
                choosenClassStudentMarkList.add(studentMarks)
            }
        }
    }

    private fun getClass_6_data() {
        for (studentData in AllData.studentDataList) {
            if (studentData.classGrade == 6) {
                choosenClassStudentDataList.add(studentData)
            }
        }
        for (studentMarks in AllData.studentMarksList) {
            if (studentMarks.classGrade == 6) {
                choosenClassStudentMarkList.add(studentMarks)
            }
        }
    }

    private fun getClass_7_data() {
        for (studentData in AllData.studentDataList) {
            if (studentData.classGrade == 7) {
                choosenClassStudentDataList.add(studentData)
            }
        }
        for (studentMarks in AllData.studentMarksList) {
            if (studentMarks.classGrade == 7) {
                choosenClassStudentMarkList.add(studentMarks)
            }
        }
    }

    private fun getClass_8_data() {
        for (studentData in AllData.studentDataList) {
            if (studentData.classGrade == 8) {
                choosenClassStudentDataList.add(studentData)
            }
        }
        for (studentMarks in AllData.studentMarksList) {
            if (studentMarks.classGrade == 8) {
                choosenClassStudentMarkList.add(studentMarks)
            }
        }
    }

    private fun setOnClickListeners() {
        setOnClickListenerForSearchEdtTxt()
        listOfStudentBtn.setOnClickListener(View.OnClickListener {
            listOfStudentRecyclerView.setVisibility(View.VISIBLE)
            resultsRecyclerView.setVisibility(View.GONE)
        })
        resultsBtn.setOnClickListener(View.OnClickListener {
            listOfStudentRecyclerView.setVisibility(View.GONE)
            resultsRecyclerView.setVisibility(View.VISIBLE)
        })
    }

    private fun setOnClickListenerForSearchEdtTxt() {}

    /////////////////////PROGRESS_BAR////////////////////////////
    fun showProgress(show: Boolean) {
        if (show) {
            Toasty.info(requireContext(), "loading...").show()
        } else {
            Toasty.info(requireContext(), "finished loading").show()
        }
    }

    private fun setUpRecyclerView() {
        //////////////////STUDENTS/////////////////////////////
        val query = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS).whereEqualTo("classGrade", class_)
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<StudentData>().setQuery(query) { snapshot ->
            val studentData = snapshot.toObject(StudentData::class.java)
            studentData!!.id = snapshot.id
            studentData
        }.setLifecycleOwner(viewLifecycleOwner).build()
        choosenClassActivityStudentsRecyclerAdapter = ChoosenClassActivityStudentsRecyclerAdapter(this, firestoreRecyclerOptions)
        binding.listOfStudentRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.listOfStudentRecyclerView.setAdapter(choosenClassActivityStudentsRecyclerAdapter)


        ////////////////////////////////RESULTS/////////////////////////
        val query2 = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS_MARKS).whereEqualTo("classGrade", class_).orderBy("totalMarks", Query.Direction.DESCENDING)
        val recyclerOptions = FirestoreRecyclerOptions.Builder<StudentMarks>().setLifecycleOwner(viewLifecycleOwner).setQuery(query2) { snapshot ->
            val studentMarks = snapshot.toObject(StudentMarks::class.java)
            studentMarks!!.position = counter++
            studentMarks.id = snapshot.id
            studentMarks
        }.build()
        choosenClassActivityResultsRecyclerAdapter = ChoosenClassActivityResultsRecyclerAdapter(this, recyclerOptions)
        resultsRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        resultsRecyclerView.setAdapter(choosenClassActivityResultsRecyclerAdapter)
    }

    override fun onResume() {
        super.onResume()
        choosenClassActivityResultsRecyclerAdapter?.notifyDataSetChanged()
        choosenClassActivityStudentsRecyclerAdapter?.notifyDataSetChanged()

    }


}



