package com.cloq.loonki.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.R

class RegionAdapter(private val list: ArrayList<String>, val click: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.region_recycler_item, parent, false)
        return RegionHolder(view, click)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = list[position]
        (holder as RegionHolder).name.text = obj
        holder.itemView.setOnClickListener {
            click(obj)
        }
    }

    inner class RegionHolder(itemView: View, click: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.region_item_text)
    }

}