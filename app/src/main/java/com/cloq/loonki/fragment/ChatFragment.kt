package com.cloq.loonki.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloq.loonki.*
import com.cloq.loonki.adapter.ChatListAdapter
import com.cloq.loonki.data.ChatRoom
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : Fragment() {
    val uid = App.prefs.getPref("UID", "")
    private var listenerRegistration : ListenerRegistration? = null
    private val chatList = arrayListOf<ChatListInfo>()
    private lateinit var adapter: ChatListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatListAdapter(context as MainActivity, chatList) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("id", it.roomId)
            intent.putExtra("roomName", it.roomName)
            intent.putExtra("users", it.joinedUser)
            intent.putExtra("type", it.type)
            intent.putExtra("valid", it.valid)
            startActivity(intent)
        }
        setListener()
        chat_list_recycler.adapter = adapter
        val lm = LinearLayoutManager(context)
        chat_list_recycler.layoutManager = lm
        chat_list_recycler.setHasFixedSize(true)
    }

    private fun setListener() {
        listenerRegistration = fs.collection("users")
            .document(uid)
            .collection("rooms")
            .orderBy("time", Query.Direction.DESCENDING)
            .whereEqualTo("deleted", false)
            .whereEqualTo("valid", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.d("파이어 스토어", "파이어스토어 채팅 리스트 에서", e)
                    return@addSnapshotListener
                }

                val list = arrayListOf<ChatListInfo>()

                snapshot?.documents?.forEach {
                    println((it["type"]))
                    val roomName = if (it["room_name"] != null) {
                        it["room_name"].toString()
                    } else {
                        ""
                    }
                    list.add(
                        ChatListInfo(
                            it.get("users") as ArrayList<String>,
                            it.id,
                            it.get("alert") as Boolean,
                            it.get("room_name").toString(),
                            ((it["type"]) as Long).toInt(),
                            it["valid"] as Boolean
                        )
                    )
                }

                if (chatList.size != 0) chatList.removeAll(chatList)
                chatList.addAll(list)

                adapter.notifyDataSetChanged()
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}