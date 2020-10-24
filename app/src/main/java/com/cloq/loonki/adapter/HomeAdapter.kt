package com.cloq.loonki.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.*
import com.cloq.loonki.MainActivity.Post
import com.cloq.loonki.fragment.storage

class HomeAdapter(private val dataList: List<Post>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        return when (viewType) {
            Post.NOTICE_CONTENT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_item_notice, parent, false)
                HomeNoticeHolder(view)
            }
            Post.MAIN_CONTENT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_item_main, parent, false)
                HomeMainHolder(view)
            }
            Post.LOADING -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.chat_loading, parent, false)
                HomeLoadingHolder(view)
            }
            else -> throw RuntimeException("뷰타입에러")
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        val data = dataList[i]
        when (data.type) {
            Post.NOTICE_CONTENT -> {
                (holder as HomeNoticeHolder).text.text = data.text
            }
            Post.MAIN_CONTENT -> {
                holder as HomeMainHolder
                holder.text.text = data.text
                fs.collection("users").document(data.uid).get()
                    .addOnSuccessListener { writer ->
                        if (!writer.exists()) return@addOnSuccessListener
                        holder.userName.text = writer["name"].toString()
                        holder.userImg.setImageDrawable(null)
                        if (writer["profile_url"] != null) {
                            GlideApp.with(holder.itemView.context)
                                .load(writer["profile_url"])
                                .override(128, 128)
                                .circleCrop().into(holder.userImg)
                        }
                    }
                holder.postImg.setImageDrawable(null)
                if (data.image) {
                    holder.postImg.visibility = View.VISIBLE
                    GlideApp.with(holder.itemView.context)
                        .load(storage.getReference("posts/${data.pid}/image.png"))
                        .into(holder.postImg)
                }
                holder.time.text = data.time

                holder.userInfo.setOnClickListener {
                    val intent = Intent(it.context, UserActivity::class.java)
                    intent.putExtra("uid", data.uid)
                    startActivity(it.context, intent, null)
                }
                holder.layout.setOnClickListener {
                    val intent = Intent(it.context, PostActivity::class.java)
                    intent.putExtra("pid", data.pid)
                    startActivity(it.context, intent, null)
                }
            }
        }
    }



    override fun getItemViewType(i: Int): Int {
        return dataList[i].type
    }

    inner class HomeMainHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.home_main_user_name)
        val userImg: ImageView = itemView.findViewById(R.id.home_main_user_image)
        val postImg: ImageView = itemView.findViewById(R.id.home_main_post_image)
        val text: TextView = itemView.findViewById(R.id.home_main_text)
        val time: TextView = itemView.findViewById(R.id.home_item_main_time)
        val userInfo: LinearLayout = itemView.findViewById(R.id.home_user_info)
        val layout: LinearLayout = itemView.findViewById(R.id.home_item_main_layout)
    }

    inner class HomeNoticeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.home_notice_text)
    }

    inner class HomeLoadingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}
}
