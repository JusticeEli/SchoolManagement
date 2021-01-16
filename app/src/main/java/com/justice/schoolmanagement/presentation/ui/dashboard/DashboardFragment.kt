package com.justice.schoolmanagement.presentation.ui.dashboard

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.google.firebase.auth.FirebaseAuth
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentDashboardBinding
import com.justice.schoolmanagement.presentation.utils.Constants
import es.dmoral.toasty.Toasty
import java.util.*


class DashboardFragment : Fragment(R.layout.fragment_dashboard), View.OnClickListener {
    private val TAG = "DashBoardActivity"

    private lateinit var binding: FragmentDashboardBinding


    private val firebaseAuth = FirebaseAuth.getInstance()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardBinding.bind(view)
        setOnClickListeners()
        setHasOptionsMenu(true)
        binding.teacherCardView!!.isVisible =Constants.isAdmin

    }

    override fun onStart() {
        super.onStart()
        if(firebaseAuth.currentUser==null){
            findNavController().navigate(R.id.action_dashboardFragment_to_splashScreenFragment)
            findNavController().popBackStack()
        }

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
            R.id.blogsMenu -> findNavController().navigate(R.id.action_dashboardFragment_to_blogFragment)
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
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