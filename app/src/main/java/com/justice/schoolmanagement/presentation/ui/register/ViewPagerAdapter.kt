/*
package com.justice.schoolmanagement.presentation.ui.register

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

import androidx.viewpager2.adapter.FragmentStateAdapter


class ViewPagerAdapter(fa: FragmentActivity?,val registerFragment: RegisterFragment) : FragmentStateAdapter(fa!!) {
     val mFragments: Array<Fragment> = arrayOf<Fragment>(

            AllFragment(registerFragment),
            PresentFragment(registerFragment),
            AbsentFragment(registerFragment)
    )
    val mFragmentNames = arrayOf( //Tabs names array
            "All",
            "Present",
            "Absent"
    )

    override fun getItemCount(): Int {
        return mFragments.size //Number of fragments displayed
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }


    override fun createFragment(position: Int): Fragment {
        return mFragments[position]
    }
}*/
