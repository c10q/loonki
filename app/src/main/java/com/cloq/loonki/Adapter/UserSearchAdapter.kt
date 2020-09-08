package com.cloq.loonki.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.MainActivity
import com.cloq.loonki.R
import com.cloq.loonki.UserInfo

class UserSearchAdapter(val context: MainActivity, private val userList: ArrayList<UserInfo>, val clickUser: (UserInfo) -> Unit ) :
    RecyclerView.Adapter<UserSearchAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchAdapter.Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_item_card, parent, false)
        return Holder(view, clickUser)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(userList[position], context)
    }

    inner class Holder(itemView: View?, clickUser: (UserInfo) -> Unit) : RecyclerView.ViewHolder(itemView!!) {
            private val userPhoto = itemView?.findViewById<ImageView>(R.id.search_card_photo)
            private val userName = itemView?.findViewById<TextView>(R.id.search_card_user_name)
            private val userAge = itemView?.findViewById<TextView>(R.id.search_card_user_age)
            private val userRegion = itemView?.findViewById<TextView>(R.id.search_card_user_region)


            fun bind (user: UserInfo, context: Context) {
                if(user.photo != "") {
                    val resourceId = context.resources.getIdentifier(user.photo, "drawable", context.packageName)
                    userPhoto?.setImageResource(resourceId)
                } else {
                    userPhoto?.setImageResource(R.mipmap.ic_launcher)
                }

                userName?.text = user.userId
                userAge?.text = user.age
                userRegion?.text = user.region

                itemView.setOnClickListener { clickUser(user) }
            }
        }
}
