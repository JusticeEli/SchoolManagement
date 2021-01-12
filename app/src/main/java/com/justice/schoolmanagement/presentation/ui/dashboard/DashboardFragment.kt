package com.justice.schoolmanagement.presentation.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.blog.BlogActivity
import com.justice.schoolmanagement.databinding.FragmentDashboardBinding
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty
import java.util.*


class DashboardFragment : Fragment(R.layout.fragment_dashboard), View.OnClickListener {
    val RC_SIGN_IN = 5
    private val TAG = "DashBoardActivity"

    private lateinit var binding: FragmentDashboardBinding

    private val teacherCardView: CardView? = null

    private val firebaseAuth = FirebaseAuth.getInstance()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardBinding.bind(view)
        setOnClickListeners()
    }


    private fun setOnClickListeners() {

        binding.apply {
            teachersBtn.setOnClickListener(this@DashboardFragment)
            parentsBtn.setOnClickListener(this@DashboardFragment)
            studentsBtn.setOnClickListener(this@DashboardFragment)
            classesBtn.setOnClickListener(this@DashboardFragment)
            subjectsBtn.setOnClickListener(this@DashboardFragment)
            resultsBtn.setOnClickListener(this@DashboardFragment)
        }


    }


    private fun recreateFragment() {
        fragmentManager
                ?.beginTransaction()
                ?.detach(this)
                ?.attach(this)
                ?.addToBackStack(null)
                ?.commit()
    }

    private fun showToast(message: String) {
        Toasty.error(requireContext(), message).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_blog, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
            R.id.blogsMenu -> startActivity(Intent(activity, BlogActivity::class.java))
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: called")
        //if user is not logged in we want to exit this method
        if (firebaseAuth.currentUser == null) {
            Log.d(TAG, "onStart: user not signed in")
            return
        }
        val collectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_TEACHERS)
        collectionReference.document(FirebaseAuth.getInstance().currentUser!!.uid).addSnapshotListener(EventListener { documentSnapshot, e ->
            if (e != null) {
                Log.d(TAG, "onEvent: Error: " + e.message)
                Toast.makeText(activity, "Error: " + e.message, Toast.LENGTH_SHORT).show()
                return@EventListener
            }
            if (!documentSnapshot!!.exists()) {
                //teacher metadata does not exit
                Log.d(TAG, "onEvent: teacher metadata does not exit going to AddTeacherActivity")
                findNavController().navigate(DashboardFragmentDirections.actionDashboardFragmentToAddTeacherFragment())
//                    Intent intent=new Intent(getContext(), AddTeacherActivity.class);
//                    startActivity(intent);
            } else {
                if (documentSnapshot.getString("type") == "teacher") {
                    //its a teacher not admin
                    Log.d(TAG, "onEvent: its a teacher not admin")
                    Constants.isAdmin = false
                    teacherCardView!!.visibility = View.GONE
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.teachersBtn -> findNavController().navigate(R.id.action_global_teachersFragment)
            R.id.parentsBtn -> findNavController().navigate(R.id.action_global_parentsFragment)
            R.id.studentsBtn -> findNavController().navigate(R.id.action_global_studentsFragment)
            R.id.classesBtn -> findNavController().navigate(R.id.action_global_classesFragment)
            R.id.subjectsBtn -> findNavController().navigate(R.id.action_global_subjectsFragment)
            R.id.resultsBtn -> findNavController().navigate(R.id.action_global_resultsFragment)

        }

    }
}