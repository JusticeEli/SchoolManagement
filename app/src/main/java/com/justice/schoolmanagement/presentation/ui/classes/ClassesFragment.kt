package com.justice.schoolmanagement.presentation.ui.classes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentClassesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_classes.*
import kotlinx.coroutines.flow.collect
@AndroidEntryPoint
class ClassesFragment : Fragment(R.layout.fragment_classes), View.OnClickListener {
    lateinit var binding: FragmentClassesBinding
    private val viewModel: ClassesViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClassesBinding.bind(view)
        setOnClickListeners()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            subScribeToObservers()
        }
    }

    private suspend fun subScribeToObservers() {
        viewModel.classesEvents.collect {
            when (it) {
                is Event.ClassChoosen -> {
                    classChoosen(it.classNumber)
                }
            }
        }
    }

    private fun classChoosen(classNumber: Int) {
        findNavController().navigate(ClassesFragmentDirections.actionClassesFragmentToChoosenClassFragment(classNumber.toString()))
    }

    private fun setOnClickListeners() {

        class_1_btn.setOnClickListener(this)
        class_2_btn.setOnClickListener(this)
        class_3_btn.setOnClickListener(this)
        class_4_btn.setOnClickListener(this)
        class_5_btn.setOnClickListener(this)
        class_6_btn.setOnClickListener(this)
        class_7_btn.setOnClickListener(this)
        class_8_btn.setOnClickListener(this)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.class_1_btn -> {
                viewModel.setEvent(Event.ClassChoosen(1))
            }
            R.id.class_2_btn -> {
                viewModel.setEvent(Event.ClassChoosen(2))
            }
            R.id.class_3_btn -> {
                viewModel.setEvent(Event.ClassChoosen(3))
            }
            R.id.class_4_btn -> {
                viewModel.setEvent(Event.ClassChoosen(4))
            }
            R.id.class_5_btn -> {
                viewModel.setEvent(Event.ClassChoosen(5))
            }
            R.id.class_6_btn -> {
                viewModel.setEvent(Event.ClassChoosen(6))
            }
            R.id.class_7_btn -> {
                viewModel.setEvent(Event.ClassChoosen(7))
            }
            R.id.class_8_btn -> {
                viewModel.setEvent(Event.ClassChoosen(8))
            }
        }
    }

    sealed class Event {
        data class ClassChoosen(val classNumber: Int) : Event()
    }
}