package com.cloq.loonki.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.cloq.loonki.*

class SettingsFragment : Fragment() {
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        listView = rootView.findViewById(R.id.setting_lists)

        val testList = arrayOf("로그아웃", "계정 설정", "계정 삭제")

        val adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, testList) }

        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

            when(parent.getItemAtPosition(position) as String) {
                "로그아웃" -> logout()
                "계정 설정" -> Toast.makeText(context, "계정 설정", Toast.LENGTH_SHORT).show()
                "계정 삭제" -> deleteAccount()
            }

        }

        return rootView
    }

    private fun logout() {
        App.prefs.setPref("UID", "")

        val intent = Intent(context, AuthActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("act", "Logout")

        startActivity(intent)
    }

    private fun deleteAccount() {
        App.prefs.setPref("UID", "")
        val intent = Intent(context, AuthActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("act", "delete")

        startActivity(intent)
    }


}