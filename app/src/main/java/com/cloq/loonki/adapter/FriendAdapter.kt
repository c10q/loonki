package com.cloq.loonki.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cloq.loonki.*
import com.cloq.loonki.dialog.FriendDialog

class FriendAdapter (private val dataList: ArrayList<String>, private val type: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_item, parent, false)
        return FriendHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
        fs.collection("users").document(dataList[i]).get().addOnSuccessListener {user ->
            (holder as FriendHolder).name.text = user["name"].toString()
            GlideApp.with(holder.itemView.context)
                .load(user["profile_url"])
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(100, 100)
                .circleCrop().into(holder.image)
            holder.layout.setOnClickListener {
                val intent = Intent(it.context, UserActivity::class.java)
                intent.putExtra("uid", user.id)
                ContextCompat.startActivity(it.context, intent, null)
            }
            val myUid = App.prefs.getPref("UID", "")
            if (myUid != ""){
                when(type) {
                    0 -> {
                        holder.btn.setOnClickListener {
                            val dlg = FriendDialog(holder.itemView.context)
                            dlg.start(dataList[i], FriendActivity.CONNECTED)
                            dlg.finish {
                                if (it)
                                    setData(dataList[i], myUid, FriendActivity.NOT_FRIEND)
                            }
                        }
                    }
                    1 -> {
                        holder.btn.setOnClickListener {
                            val dlg = FriendDialog(holder.itemView.context)
                            dlg.start(dataList[i], FriendActivity.REQUEST_ACCEPT)
                            dlg.finish {
                                if (it)
                                    setData(dataList[i], myUid, FriendActivity.CONNECTED)
                                else
                                    setData(dataList[i], myUid, FriendActivity.NOT_FRIEND)
                            }
                        }
                    }
                }
            }

        }


    }

    private fun setData(uid: String, myUid: String, status: Int) {
        val data = hashMapOf(
            "status" to status
        ) as Map<String, Any>

        fs.collection("users").document(uid)
            .collection("friends").document(myUid).update(data)
        fs.collection("users").document(myUid)
            .collection("friends").document(uid).update(data)
    }

    inner class FriendHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.friend_item_user_image)
        var name: TextView = itemView.findViewById(R.id.friend_item_user_name)
        var layout: LinearLayout = itemView.findViewById(R.id.friend_item_layout)
        val btn: Button = itemView.findViewById(R.id.friend_item_btn)
    }

}