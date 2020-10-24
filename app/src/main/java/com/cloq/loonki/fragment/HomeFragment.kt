package com.cloq.loonki.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.cloq.loonki.*
import com.cloq.loonki.adapter.HomeAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    private lateinit var adapter: HomeAdapter
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        home_swipe_layout.setOnRefreshListener {
            home_swipe_layout.isRefreshing = false
        }

        uid = App.prefs.getPref("UID", "")
        getData()

    }

    private fun getData() {
        fs.collection("user").document(uid).collection("friends").get().addOnSuccessListener {friends ->
            friends.documents.forEach {friend ->
                fs.collection("users").document(friend.id)
            }
        }
    }

    data class PID(val pid: String, val time: Long)
}