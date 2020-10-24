package com.cloq.loonki.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.cloq.loonki.fragment.*

class BottomNavAdapter(fm : FragmentManager, private val fragmentCount : Int) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> MyPageFragment()
            1 -> ChannelFragment()
            2 -> HomeFragment()
            3 -> ChatFragment()
            4 -> SettingsFragment()
            else -> HomeFragment()
        }
    }

    override fun getCount(): Int = fragmentCount

}