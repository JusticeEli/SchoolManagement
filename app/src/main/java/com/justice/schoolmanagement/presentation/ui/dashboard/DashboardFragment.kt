package com.justice.schoolmanagement.presentation.ui.dashboard

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentDashboardBinding
import com.justice.schoolmanagement.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import java.util.*

@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.fragment_dashboard), View.OnClickListener {
    private val TAG = "DashBoardActivity"

    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardBinding.bind(view)
        setOnClickListeners()
        setHasOptionsMenu(true)
        subScribeToObservers()
    }

    private fun subScribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {

            viewModel.dashBoardEvents.collect {
                when (it) {
                    Event.TeacherClicked -> findNavController().navigate(R.id.action_global_teachersFragment)
                    Event.ParentClicked -> findNavController().navigate(R.id.action_global_parentsFragment)
                    Event.StudentClicked -> findNavController().navigate(R.id.action_global_studentsFragment)
                    Event.ClassesClicked -> findNavController().navigate(R.id.action_global_classesFragment)
                    Event.SubjectsClicked -> findNavController().navigate(R.id.action_global_subjectsFragment)
                    Event.ResultsClicked -> findNavController().navigate(R.id.action_global_resultsFragment)
                }.exhaustive
            }

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
            R.id.teachersBtn -> viewModel.setEvent(Event.TeacherClicked)
            R.id.parentsBtn -> viewModel.setEvent(Event.ParentClicked)
            R.id.studentsBtn -> viewModel.setEvent(Event.StudentClicked)
            R.id.classesBtn -> viewModel.setEvent(Event.ClassesClicked)
            R.id.subjectsBtn -> viewModel.setEvent(Event.SubjectsClicked)
            R.id.resultsBtn -> viewModel.setEvent(Event.ResultsClicked)

        }
    }

    sealed class Event {
        object TeacherClicked : Event()
        object ParentClicked : Event()
        object StudentClicked : Event()
        object ClassesClicked : Event()
        object SubjectsClicked : Event()
        object ResultsClicked : Event()
    }
}