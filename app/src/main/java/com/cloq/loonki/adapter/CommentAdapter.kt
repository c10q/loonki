package com.cloq.loonki.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.GlideApp
import com.cloq.loonki.PostActivity.Comment
import com.cloq.loonki.R
import com.cloq.loonki.fs


class CommentAdapter (private val commentList: ArrayList<Comment>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        return when (viewType) {
            Comment.MAIN -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.comment_item_main, parent, false
                )
                CommentMainHolder(view)
            }
            else -> throw RuntimeException("뷰타입에러")
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        val data = commentList[i]
        when(data.type) {
            Comment.MAIN -> {
                (holder as CommentMainHolder).text.text = data.text
                fs.collection("users").document(data.writer).get().addOnSuccessListener {writer ->
                    holder.name.text = writer["name"].toString()
                    holder.image.setImageDrawable(null)
                    GlideApp.with(holder.itemView.context).load(
                        writer["profile_url"]
                    ).circleCrop().into(holder.image)
                }
                holder.time.text = getTimeDiff(data.time)
            }
        }
    }

    private fun getTimeDiff(time: Long): String {
        val t = (System.currentTimeMillis() - time) / 1000

        return when {
            (t < 60) -> {
                t.toString() + "초 전"
            }
            (t < 60 * 60) -> {
                (t / 60).toString() + "분 전"
            }
            (t < 3600 * 60 * 60) -> {
                (t / (60 * 60)).toString() + "시간 전"
            }
            (t < 3600 * 60 * 60 * 24) -> {
                (t / (60 * 60 * 24)).toString() + "일 전"
            }
            else -> {
                (t / (60 * 60 * 24 * 365)).toString() + "년 전"
            }
        }
    }

    override fun getItemViewType(i: Int): Int {
        return commentList[i].type
    }

    inner class CommentMainHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.comment_text)
        val image: ImageView = itemView.findViewById(R.id.comment_user_image)
        val time: TextView = itemView.findViewById(R.id.comment_item_main_time)
        val name: TextView = itemView.findViewById(R.id.comment_user_name)
    }
}