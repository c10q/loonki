package com.cloq.loonki.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.R
import com.cloq.loonki.RegionTag

class RegionTagListAdapter(private val list: ArrayList<RegionTag>, val click: (RegionTag) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.region_recycler_item, parent, false)
        return RegionTagHolder(view, click)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = list[position]
        (holder as RegionTagListAdapter.RegionTagHolder).name.text = obj.name
        holder.itemView.setOnClickListener {
            click(obj)
        }
    }

    inner class RegionTagHolder(itemView: View, click: (RegionTag) -> Unit) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.region_item_text)
    }
}

