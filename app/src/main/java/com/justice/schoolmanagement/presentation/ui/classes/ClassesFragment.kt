package com.justice.schoolmanagement.presentation.ui.classes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.FragmentClassesBinding
import kotlinx.android.synthetic.main.fragment_classes.*

class ClassesFragment : Fragment(R.layout.fragment_classes), View.OnClickListener {
    lateinit var binding: FragmentClassesBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClassesBinding.bind(view)
        setOnClickListeners()
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

                findNavController().navigate(ClassesFragmentDirections.actionClassesFragmentToChoosenClassFragment(1))
            }
            R.id.class_2_btn -> {
                findNavController().navigate(ClassesFragmentDirections.actionClassesFragmentToChoosenClassFragment(2))
            }
            R.id.class_3_btn -> {
                findNavController().navigate(ClassesFragmentDirections.actionClassesFragmentToChoosenClassFragment(3))
            }
            R.id.class_4_btn -> {
                findNavController().navigate(ClassesFragmentDirections.actionClassesFragmentToChoosenClassFragment(4))
            }
            R.id.class_5_btn -> {
                findNavController().navigate(ClassesFragmentDirections.actionClassesFragmentToChoosenClassFragment(5))
            }
            R.id.class_6_btn -> {
                findNavController().navigate(ClassesFragmentDirections.actionClassesFragmentToChoosenClassFragment(6))
            }
            R.id.class_7_btn -> {
                findNavController().navigate(ClassesFragmentDirections.actionClassesFragmentToChoosenClassFragment(7))
            }
            R.id.class_8_btn -> {
                findNavController().navigate(ClassesFragmentDirections.actionClassesFragmentToChoosenClassFragment(8))
            }
        }
    }
}