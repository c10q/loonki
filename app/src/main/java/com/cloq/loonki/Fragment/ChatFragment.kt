package com.cloq.loonki.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloq.loonki.Adapter.ChatListAdapter
import com.cloq.loonki.ChatListInfo
import com.cloq.loonki.MainActivity
import com.cloq.loonki.R
import kotlinx.android.synthetic.main.fragment_chat.*

var chatList = arrayListOf<ChatListInfo>(
    ChatListInfo(arrayOf("노관옥"), "users_1", "시발ㄴ놈아"),
    ChatListInfo(arrayOf("차두리"), "users_2", "ㄹㅇㅋㅋ")
)

class ChatFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mAdapter = ChatListAdapter(context as MainActivity, chatList) {
            chat -> Toast.makeText(context, "클릭", Toast.LENGTH_SHORT).show()
        }
        chat_list_recycler.adapter = mAdapter

        val lm = LinearLayoutManager(context)
        chat_list_recycler.layoutManager = lm
        chat_list_recycler.setHasFixedSize(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }
}