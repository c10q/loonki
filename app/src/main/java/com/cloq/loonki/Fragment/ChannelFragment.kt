package com.cloq.loonki.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager


import com.cloq.loonki.Adapter.ChannelListAdapter
import com.cloq.loonki.Channel
import com.cloq.loonki.MainActivity
import com.cloq.loonki.R
import com.cloq.loonki.RandomChannelActivity
import kotlinx.android.synthetic.main.fragment_channel.*

const val CHANNEL_RANDOM: String = "Random"

var channelList = arrayListOf<Channel>(
    Channel(CHANNEL_RANDOM, "anonymous"),
    Channel("channel_1", ""),
    Channel("channel_2", ""),
    Channel("channel_3", ""),
    Channel("channel_4", ""),
    Channel("channel_5", "")
)

class ChannelFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            val mAdapter = ChannelListAdapter(this.context as MainActivity, channelList) {
                    channel -> when(channel.name) {
                CHANNEL_RANDOM -> {
                    val randomIntent = Intent(context, RandomChannelActivity::class.java)
                    startActivity(randomIntent)
                }
                else -> {
                    Toast.makeText(context, "클릭이벤트 ${channel.name}", Toast.LENGTH_SHORT).show()
                }
            }

            }
            channel_recycler_view.adapter = mAdapter

            val gm = GridLayoutManager(context, 3)
            channel_recycler_view.layoutManager = gm
            channel_recycler_view.setHasFixedSize(true)
    }
}

