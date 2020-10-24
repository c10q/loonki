package com.cloq.loonki

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.adapter.RegionTagCardAdapter
import com.cloq.loonki.adapter.RegionTagListAdapter
import kotlinx.android.synthetic.main.activity_region_tag.*


val regions: ArrayList<RegionTag> = arrayListOf()
val areas: ArrayList<RegionTag> = arrayListOf()
val tagbk: ArrayList<TagInfo> = arrayListOf()

data class RegionTag(val name: String, var selected: Boolean = false) {}
data class TagInfo(val region: String, val area: String) {}

class RegionTagActivity : AppCompatActivity() {
    var flag: Boolean = true
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_region_tag)
        region_card_cancel_btn.setOnClickListener {
            super.onBackPressed()
        }

        save_random_region_btn.setOnClickListener {
            tags.removeAll(tags)
            tags.addAll(tagbk)
            super.onBackPressed()
        }
        tagbk.removeAll(tagbk)
        tagbk.addAll(tags)

        getRegionList()

        val a = RegionTagCardAdapter(tagbk) {
            tagbk.remove(it)
            region_card_recycler.layoutManager =
                GridLayoutManager(this, 4, RecyclerView.HORIZONTAL, false)
        }
        region_card_recycler.layoutManager =
            GridLayoutManager(this, 4, RecyclerView.HORIZONTAL, false)
        region_card_recycler.adapter = a
    }

    override fun onDestroy() {
        super.onDestroy()
        regions.removeAll(regions)
        areas.removeAll(areas)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getRegionList() {
        regions.add(
            RegionTag(
                "전체", true
            )
        )
        fs.collection("regions").orderBy("name").get().addOnSuccessListener { q ->
            q.documents.forEach {
                regions.add(
                    RegionTag(it.get("name").toString(), false)
                )
            }

            val rAdapter = RegionTagListAdapter(regions) {
                if (flag)
                getAreaList(it.name)
            }
            region_tag_recycler_view.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            region_tag_recycler_view.adapter = rAdapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getAreaList(region: String) {
        areas.removeAll(areas)
        areas.add(RegionTag("전체", true))
        if (flag) {
            flag = false
            fs.collection("regions").whereEqualTo("name", region).get().addOnSuccessListener { q ->
                var cnt = 0
                q.documents.forEach { d ->
                    (d.get("area") as ArrayList<String>).forEach {
                        areas.add(
                            RegionTag(
                                it, false
                            )
                        )
                    }
                    cnt++
                }
                if (q.documents.size == cnt) flag = true
                val aAdapter = RegionTagListAdapter(areas) {
                    addTag(region, it.name)
                }
                area_tag_recycler_view.layoutManager =
                    LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                area_tag_recycler_view.adapter = aAdapter
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun addTag(region: String, area: String) {
        val tag = TagInfo(region, area)
        when {
            (region == "전체") -> {
                tagbk.removeAll(tagbk)
            }
            (tagbk.contains(TagInfo("전체", "전체"))) -> {
                tagbk.remove(TagInfo("전체", "전체"))
            }
            (area == "전체") -> {
                tagbk.removeIf { n -> n.region == region }
            }
            (tagbk.contains(TagInfo(region, "전체"))) -> {
                tagbk.remove(TagInfo(region, "전체"))
            }
            (tagbk.contains(tag)) -> {
                Toast.makeText(this, "이미 추가된 지역입니다.", Toast.LENGTH_SHORT).show()
                return
            }
            (tagbk.size >= 12) -> {
                Toast.makeText(this, "최대 12개까지 추가 가능합니다.", Toast.LENGTH_SHORT).show()
                return
            }
        }
        tagbk.add(tag)
        val tAdapter = RegionTagCardAdapter(tagbk) {
            tagbk.remove(it)
            region_card_recycler.layoutManager =
                GridLayoutManager(this, 4, RecyclerView.HORIZONTAL, false)
        }
        region_card_recycler.layoutManager =
            GridLayoutManager(this, 4, RecyclerView.HORIZONTAL, false)
        region_card_recycler.adapter = tAdapter
    }
}

