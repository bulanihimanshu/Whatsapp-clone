package com.example.whatsappclone.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whatsappclone.fragments.CallsFragment
import com.example.whatsappclone.fragments.ChatsFragment
import com.example.whatsappclone.fragments.StatusFragment

class TabAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChatsFragment()
            1 -> StatusFragment()
            2 -> CallsFragment()
            else -> ChatsFragment()
        }
    }
}
