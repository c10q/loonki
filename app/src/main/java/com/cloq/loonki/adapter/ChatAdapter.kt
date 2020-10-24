package com.cloq.loonki.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cloq.loonki.*
import com.cloq.loonki.data.ChatRoom
import com.cloq.loonki.dialog.ChatLeftDialog
import com.cloq.loonki.dialog.ChatRightDialog
import kotlinx.android.synthetic.main.chat_center.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val dataList: MutableList<Model>,
    private val roomType: Int,
    private val roomID: String,
    private val uid: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        return when (viewType) {
            Model.LEFT_VIEW -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_left_first, parent, false)
                LeftViewHolder(view)
            }
            Model.RIGHT_VIEW -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_right, parent, false)
                RightViewHolder(view)
            }
            Model.CENTER_VIEW -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_center, parent, false)
                CenterViewHolder(view)
            }
            Model.LOADING_VIEW -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> throw RuntimeException("뷰타입에러")
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    private fun getProfileImg(holder: LeftViewHolder) {
        when (roomType) {
            ChatRoom.ROOM_BLACK -> {
                GlideApp.with(holder.itemView.context)
                    .load("https://firebasestorage.googleapis.com/v0/b/loonki.appspot.com/o/loonki%2Floonki_black.png?alt=media&token=88ecd5fd-26d9-46c6-8730-aabf6e1d5d2a")
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(100, 100)
                    .circleCrop().into(holder.img)
            }
            ChatRoom.ROOM_WHITE -> {
                GlideApp.with(holder.itemView.context)
                    .load("https://firebasestorage.googleapis.com/v0/b/loonki.appspot.com/o/loonki%2Floonki_white.png?alt=media&token=9ab94c89-b949-438c-84bc-1897791175ed")
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(100, 100)
                    .circleCrop().into(holder.img)
            }
            ChatRoom.ROOM_NORMAL -> {
                fs.collection("users").document(uid).get().addOnSuccessListener {
                    GlideApp.with(holder.itemView.context)
                        .load(it["profile_url"])
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(100, 100)
                        .circleCrop().into(holder.img)
                    holder.img.setOnClickListener{
                        val intent = Intent(holder.itemView.context, UserActivity::class.java)
                        intent.putExtra("uid", uid)
                        ContextCompat.startActivity(holder.itemView.context, intent, null)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = dataList[position]
        when (obj.type) {
            Model.LEFT_VIEW -> {
                (holder as LeftViewHolder).message.text = if (!obj.deleted) obj.message else "삭제됨."

                holder.img.setImageDrawable(null)

                when {
                    position == 0 -> getProfileImg(holder)
                    dataList[position - 1].type == Model.RIGHT_VIEW -> getProfileImg(holder)
                }

                holder.time.text = SimpleDateFormat("HH:mm", Locale.KOREA).format(obj.stamp)
                holder.itemView.setOnLongClickListener {
                    val dlg = ChatLeftDialog(holder.itemView.context)
                    dlg.start(obj.message.toString())
                    true
                }
            }
            Model.RIGHT_VIEW -> {
                (holder as RightViewHolder).message.text = if (!obj.deleted) obj.message else "삭제됨."
                holder.time.text = SimpleDateFormat("HH:mm", Locale.KOREA).format(obj.stamp)
                holder.itemView.setOnLongClickListener {
                    val dlg = ChatRightDialog(holder.itemView.context)
                    dlg.start(obj.message.toString(), obj.chatID, roomID)
                    true
                }
            }
            Model.CENTER_VIEW -> {
                (holder as CenterViewHolder).message.text =
                    SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(obj.stamp)
            }
        }
    }

    fun getHeaderLayoutView(item: RecyclerView, position: Int): View? {
        val lastIndex = if (position < dataList.size) position else dataList.size - 1
        for (index in lastIndex downTo 0) {
            val view: View = LayoutInflater.from(item.context)
                .inflate(R.layout.chat_center, item, false)
            view.message_center.text =
                SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(dataList[position].stamp)
            return view
        }
        return null
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].type
    }

    fun isHolder(position: Int): Boolean {
        return dataList[position].type == Model.CENTER_VIEW
    }

    inner class LeftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var message: TextView = itemView.findViewById(R.id.message_left)
        var time: TextView = itemView.findViewById(R.id.chat_left_time)
        var img: ImageView = itemView.findViewById(R.id.chat_left_img)
    }

    inner class RightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var message: TextView = itemView.findViewById(R.id.message_right)
        var time: TextView = itemView.findViewById(R.id.chat_right_time)
    }

    inner class CenterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var message: TextView = itemView.findViewById(R.id.message_center)
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}