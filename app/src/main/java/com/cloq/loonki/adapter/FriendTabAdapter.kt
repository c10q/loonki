package com.cloq.loonki.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.cloq.loonki.fragment.FriendListFragment

class FriendTabAdapter (fm: FragmentManager): FragmentStatePagerAdapter(fm){
    private var fragments : ArrayList<FriendListFragment> = ArrayList()

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    fun addItems(fragment : FriendListFragment){
        fragments.add(fragment)
    }
}