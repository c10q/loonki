package com.cloq.loonki.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.MainActivity
import com.cloq.loonki.R
import com.cloq.loonki.Channel

class ChannelListAdapter(val context: MainActivity, private val channelList: ArrayList<Channel>, val clickChannel: (Channel) -> Unit) :
    RecyclerView.Adapter<ChannelListAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelListAdapter.Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.channel_item, parent, false)
        return Holder(view, clickChannel)
    }

    override fun getItemCount(): Int {
        return channelList.size
    }

    override fun onBindViewHolder(holder: ChannelListAdapter.Holder, position: Int) {
        holder.bind(channelList[position], context)
    }

    inner class Holder(itemView: View?, clickChannel: (Channel) -> Unit) : RecyclerView.ViewHolder(itemView!!) {
        private val channelImg = itemView?.findViewById<ImageView>(R.id.channel_card_image)
        private val channelTitle = itemView?.findViewById<TextView>(R.id.channel_item_title)


        fun bind (channel: Channel, context: Context) {
            if(channel.image != "") {
                val resourceId = context.resources.getIdentifier(channel.image, "drawable", context.packageName)
                channelImg?.setImageResource(resourceId)
            } else {
                channelImg?.setImageResource(R.mipmap.ic_launcher)
            }

            channelTitle?.text = channel.name

            itemView.setOnClickListener { clickChannel(channel) }
        }
    }


}