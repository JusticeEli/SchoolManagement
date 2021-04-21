package com.justice.schoolmanagement.presentation.ui.register

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.ChangeEventListener
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentPresentBinding
import com.justice.schoolmanagement.presentation.ui.register.RegisterFragment.Companion.currentInfo
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData
import com.justice.schoolmanagement.presentation.utils.Constants
import kotlinx.android.synthetic.main.fragment_present.*

class PresentFragment(val registerFragment: RegisterFragment) : Fragment(R.layout.fragment_present) {
    companion object {
        private const val TAG = "PresentFragment"
    }

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var registerAdapter: RegisterAdapter2
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

    fun setUpFirestore() {
        if (binding.swipeRefreshLayout.isRefreshing){
            Log.d(TAG, "setUpFirestore: Data is already refreshing")
            return
        }

        firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.DATE).document(currentInfo.currentDate).get().addOnSuccessListener { documentsnapshot ->
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

            swipeRefreshLayout?.isRefreshing = false

        }
    }

    private fun setSwipeRefreshListener() {
    //    binding.swipeRefreshLayout.
        binding.swipeRefreshLayout.setOnRefreshListener {
           registerFragment.setUpViewPager(1)
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

        if (view==null){
            return
        }
        val query: Query
        if (currentInfo.currentClass.equals("all")) {
            query = documentsnapshot?.reference?.collection(Constants.STUDENTS)!!.whereEqualTo("present", true)

        } else {
            query = documentsnapshot?.reference?.collection(Constants.STUDENTS)!!.whereEqualTo("currentClass", currentInfo.currentClass).whereEqualTo("present", true)

        }

        val firestoreRecyclerOptions: FirestoreRecyclerOptions<StudentRegistrationData> = FirestoreRecyclerOptions.Builder<StudentRegistrationData>().setQuery(query, StudentRegistrationData::class.java).setLifecycleOwner(viewLifecycleOwner).build()

        registerAdapter = RegisterAdapter2(this, firestoreRecyclerOptions)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = registerAdapter
        registerAdapter.snapshots.addChangeEventListener(object : ChangeEventListener {
            override fun onChildChanged(type: ChangeEventType, snapshot: DocumentSnapshot, newIndex: Int, oldIndex: Int) {
                Log.d(TAG, "onChildChanged: ")

            }

            override fun onDataChanged() {
                Log.d(TAG, "onDataChanged: ")
                registerFragment.sendPresentFragmentSize(registerAdapter.snapshots.size)
            }

            override fun onError(e: FirebaseFirestoreException) {
            }
        })
    }


    private fun startFetchingData(documentsnapshot: DocumentSnapshot?) {
        if(view!=null){
            docucumentExist(documentsnapshot)

        }
       firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.STUDENTS).get().addOnCompleteListener { task ->

            if (task.isSuccessful) {

                task.result?.forEach { queryDocumentSnapshot ->

                    val studentData = queryDocumentSnapshot.toObject(StudentData::class.java)

                    val studentRegistrationData = StudentRegistrationData(queryDocumentSnapshot.id, true, studentData.classGrade.toString(), studentData)

                    firebaseFirestore.collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.DATE).document(currentInfo.currentDate).collection(Constants.STUDENTS).add(studentRegistrationData).addOnCompleteListener {
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