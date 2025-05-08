package com.example.kai_content.ui.operator.complaint

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ComplaintViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AssignToMeFragment()
            1 -> RiwayatFragment()
            else -> AssignToMeFragment()
        }
    }
}
