package com.justice.schoolmanagement.presentation.ui.register

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentPresentBinding
import com.justice.schoolmanagement.presentation.ui.register.RegisterFragment.Companion.currentInfo
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.utils.Constants
import kotlinx.android.synthetic.main.fragment_present.*

class PresentFragment : Fragment(R.layout.fragment_present) {
    companion object {
        private const val TAG = "PresentFragment"
    }

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var registerAdapter: RegisterAdapter
    lateinit var binding: FragmentPresentBinding


    lateinit var progressBar: ProgressBar
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPresentBinding.bind(view)

       setSwipeRefreshListener()


        initProgressBar()
        binding.recyclerView.setHasFixedSize(true)
        if (FirebaseAuth.getInstance().currentUser != null) {
            setUpFirestore()
        }


    }

    private fun setUpFirestore() {
        firebaseFirestore.collection(Constants.COLLECTION_DATE).document(currentInfo.currentDate).get().addOnSuccessListener { documentsnapshot ->
            if (documentsnapshot.exists()) {
                docucumentExist(documentsnapshot)
                Log.d(TAG, "setUpFirestore: document exists")
            } else {
                val map = mapOf<String, String>("currentDate" to currentInfo.currentDate)
                documentsnapshot.reference.set(map).addOnSuccessListener {
                    startFetchingData(documentsnapshot)


                    Log.d(TAG, "setUpFirestore: document doesnt exit")
                }
            }


        }
    }    private fun setSwipeRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            setUpFirestore()
            swipeRefreshLayout.isRefreshing=false
        }
    }


    private fun docucumentExist(documentsnapshot: DocumentSnapshot?) {
        ///delete
        /*    documentsnapshot?.reference?.collection(Constants.COLLECTION_STUDENTS)!!.whereEqualTo("currentClass", currentInfo.currentClass).get().addOnSuccessListener {
                it.forEach {
                    it.reference.delete().addOnSuccessListener { }
                }
            }
    */
        ///delete
        val query: Query
        if (currentInfo.currentClass.equals("all")) {
            query = documentsnapshot?.reference?.collection(Constants.COLLECTION_STUDENTS)!!.whereEqualTo("present", true)

        } else {
            query = documentsnapshot?.reference?.collection(Constants.COLLECTION_STUDENTS)!!.whereEqualTo("currentClass", currentInfo.currentClass).whereEqualTo("present", true)

        }

        val firestoreRecyclerOptions: FirestoreRecyclerOptions<StudentRegistrationData> = FirestoreRecyclerOptions.Builder<StudentRegistrationData>().setQuery(query, StudentRegistrationData::class.java).setLifecycleOwner(viewLifecycleOwner).build()

        registerAdapter = RegisterAdapter(this, firestoreRecyclerOptions)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = registerAdapter

    }


    private fun startFetchingData(documentsnapshot: DocumentSnapshot?) {
        docucumentExist(documentsnapshot)
        firebaseFirestore.collection(Constants.COLLECTION_STUDENTS).get().addOnCompleteListener { task ->

            if (task.isSuccessful) {

                task.result?.forEach { queryDocumentSnapshot ->

                    val studentData = queryDocumentSnapshot.toObject(StudentData::class.java)

                    val studentRegistrationData = StudentRegistrationData(queryDocumentSnapshot.id, true, studentData.classGrade.toString(), studentData)

                    firebaseFirestore.collection(Constants.COLLECTION_DATE).document(currentInfo.currentDate).collection(Constants.COLLECTION_STUDENTS).add(studentRegistrationData).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "startFetchingData: success adding student registration data")

                        } else {
                            Log.d(TAG, "startFetchingData: Error: ${it.exception?.message}")
                        }
                    }
                }


            } else {
                Log.d(TAG, "startFetchingData: Error: ${task.exception?.message}")
            }


        }
    }


    /////////////////////PROGRESS_BAR////////////////////////////
    fun showProgress(show: Boolean) {
        progressBar.isVisible = show
    }

    fun initProgressBar() {
        progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        binding.relativeLayout.addView(progressBar, params)
        progressBar.isVisible = false
    }

}