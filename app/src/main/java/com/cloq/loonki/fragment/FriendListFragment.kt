package com.cloq.loonki.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.App
import com.cloq.loonki.R
import com.cloq.loonki.fs
import kotlinx.android.synthetic.main.fragment_friend_list.*
import com.cloq.loonki.FriendActivity.FriendStatus
import com.cloq.loonki.adapter.FriendAdapter


private const val TYPE = "type"

class FriendListFragment() : Fragment() {
    private var type: String? = null
    private val friendList = arrayListOf<String>()
    private lateinit var adapter: FriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString(TYPE)
        }
        val uid = App.prefs.getPref("UID", "")
        val friendRef = fs.collection("users").document(uid).collection("friends")
        when (type) {
            "friend" -> {
                friendRef.whereEqualTo("status", FriendStatus.CONNECTED).get()
                    .addOnSuccessListener { friends ->
                        friends.documents.forEach { friend ->
                            friendList.add(friend.id)
                        }
                        if (friendList.size != 0) onAdapter(0)
                    }
            }
            "new" -> {
                friendRef.whereEqualTo("status", FriendStatus.REQUEST_SENT).get()
                    .addOnSuccessListener { friends ->
                        friends.documents.forEach { friend ->
                            friendList.add(friend.id)
                        }
                        if (friendList.size != 0) onAdapter(1)
                    }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_list, container, false)
    }

    private fun onAdapter(type: Int) {
        adapter = FriendAdapter(friendList, type)
        friend_list_recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        friend_list_recycler.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            FriendListFragment().apply {
                arguments = Bundle().apply {
                    putString(TYPE, type)
                }
            }
    }
}