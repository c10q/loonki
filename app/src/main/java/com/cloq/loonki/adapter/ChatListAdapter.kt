package com.cloq.loonki.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.*
import com.cloq.loonki.data.ChatRoom
import com.cloq.loonki.dialog.ChatListDialog
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatListAdapter(
    val context: MainActivity,
    private val chatList: ArrayList<ChatListInfo>,
    val clickChat: (ChatListInfo) -> Unit
) :
    RecyclerView.Adapter<ChatListAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false)
        return Holder(view, clickChat)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatListAdapter.Holder, position: Int) {
        holder.bind(chatList[position], context)
    }

    inner class Holder(itemView: View, clickChat: (ChatListInfo) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val joinedUser: TextView = itemView.findViewById(R.id.chat_joined_user)
        private val chatPhoto: ImageView = itemView.findViewById<ImageView>(R.id.chat_list_image)
        private val message: TextView = itemView.findViewById(R.id.chat_list_message)
        private val alertImg: ImageView = itemView.findViewById<ImageView>(R.id.chat_list_alert_off)
        private val time: TextView = itemView.findViewById(R.id.chat_list_last_chat_time)
        private val new: TextView = itemView.findViewById(R.id.chat_list_new)

        fun bind(chat: ChatListInfo, context: Context) {
            alertImg.visibility = if (chat.alert) View.GONE
            else View.VISIBLE
            chatPhoto.setImageDrawable(null)
            when (chat.type) {
                ChatRoom.ROOM_WHITE -> {
                    joinedUser.text = chat.roomName
                    GlideApp.with(context)
                        .load("https://firebasestorage.googleapis.com/v0/b/loonki.appspot.com/o/loonki%2Floonki_white.png?alt=media&token=9ab94c89-b949-438c-84bc-1897791175ed")
                        .override(128, 128)
                        .circleCrop().into(
                            chatPhoto
                        )
                }
                ChatRoom.ROOM_BLACK -> {
                    joinedUser.text = chat.roomName
                    GlideApp.with(context)
                        .load("https://firebasestorage.googleapis.com/v0/b/loonki.appspot.com/o/loonki%2Floonki_black.png?alt=media&token=88ecd5fd-26d9-46c6-8730-aabf6e1d5d2a")
                        .override(128, 128)
                        .circleCrop().into(
                            chatPhoto
                        )
                }
                ChatRoom.ROOM_NORMAL -> {
                    fs.collection("users").document(chat.joinedUser[0]).get()
                        .addOnSuccessListener { user ->
                            joinedUser.text = user["name"].toString()
                            GlideApp.with(context)
                                .load(user["profile_url"])
                                .override(128, 128)
                                .circleCrop().into(
                                    chatPhoto
                                )
                        }
                }
                else -> {
                    fs.collection("users").document(chat.joinedUser[0]).get()
                        .addOnSuccessListener { user ->
                            joinedUser.text = user["name"].toString()
                            GlideApp.with(context)
                                .load(user["profile_url"])
                                .override(128, 128)
                                .circleCrop().into(
                                    chatPhoto
                                )
                        }
                }
            }

            fs.collection("rooms").document(chat.roomId).collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                .addSnapshotListener { snapshot, e ->
                    snapshot?.documents?.forEach {
                        message.text = it.get("message").toString()
                        time.text =
                            SimpleDateFormat("HH:mm", Locale.KOREA).format(it.get("time") as Long)
                        if (!(it.get("read") as Boolean) && it.get("sender") != App.prefs.getPref(
                                "UID",
                                ""
                            )
                        ) new.visibility = View.VISIBLE
                    }
                }

            itemView.setOnClickListener { clickChat(chat) }
            itemView.setOnLongClickListener {
                val dlg = ChatListDialog(context)
                if (chat.alert) dlg.start("WHITE", chat.roomId, chat.alert)
                else dlg.start("WHITE", chat.roomId, chat.alert)
                true
            }
        }
    }

}