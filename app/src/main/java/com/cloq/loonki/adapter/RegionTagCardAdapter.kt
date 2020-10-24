package com.cloq.loonki.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.R
import com.cloq.loonki.RegionTag
import com.cloq.loonki.TagInfo

class RegionTagCardAdapter(private val list: ArrayList<TagInfo>, val bv: Boolean = true, val click: (TagInfo) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.region_tag_card, parent, false)
        return RegionTagHolder(view, click)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = list[position]
        (holder as RegionTagCardAdapter.RegionTagHolder).region.text = obj.region
        if (obj == TagInfo("전체", "전체")) {
            holder.area.text = ""
        } else {
            holder.area.text = obj.area
        }
        if (!bv) holder.btn.visibility = View.GONE
        holder.btn.setOnClickListener {
            click(obj)
        }
    }

    inner class RegionTagHolder(itemView: View, click: (TagInfo) -> Unit) : RecyclerView.ViewHolder(itemView) {
        var region: TextView = itemView.findViewById(R.id.region_tag_card_region)
        var area: TextView = itemView.findViewById(R.id.region_tag_card_area)
        var btn: AppCompatImageButton = itemView.findViewById(R.id.region_tag_card_clear)
    }
}