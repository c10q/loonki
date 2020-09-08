package com.cloq.loonki.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.ChatListInfo
import com.cloq.loonki.MainActivity
import com.cloq.loonki.R

class ChatListAdapter(val context: MainActivity, val chatList: ArrayList<ChatListInfo>, val clickChat: (ChatListInfo) -> Unit) :
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

    inner class Holder(itemView: View, clickChat: (ChatListInfo) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val joinedUser: TextView = itemView.findViewById(R.id.chat_joined_user)
        private val chatPhoto: ImageView = itemView.findViewById<ImageView>(R.id.chat_list_image)
        private val message: TextView = itemView.findViewById(R.id.chat_list_message)

        fun bind (chat: ChatListInfo, context: Context) {
            if(chat.photo != "") {
                val resourceId = context.resources.getIdentifier(chat.photo, "drawable", context.packageName)
                chatPhoto.setImageResource(resourceId)
            } else {
                chatPhoto.setImageResource(R.mipmap.ic_launcher)
            }
            joinedUser.text = arrToTxt(chat.joinedUser)
            message.text = chat.message

            itemView.setOnClickListener{ clickChat(chat)}
        }
    }

}

fun arrToTxt(arr: Array<String>): String {
    var str = ""
    for(i in arr){
        str = if(arr[0] == i){
            i
        } else {
            "$str, $i"
        }
    }
    return str
}