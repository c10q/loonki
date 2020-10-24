package com.cloq.loonki.dialog

import android.app.Dialog
import android.content.Context
import android.nfc.Tag
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.App
import com.cloq.loonki.R
import com.cloq.loonki.TagInfo
import com.cloq.loonki.adapter.RegionAdapter
import com.cloq.loonki.fs
import kotlinx.android.synthetic.main.region_dialog.*

class RegionDialog(context: Context, type: Int = 0) {
    private val dlg = Dialog(context)
    private val DLG_TYPE = type
    private var regions = arrayListOf<String>("선택 안함")
    private var area = arrayListOf<String>("전체")
    private var flag: Int = 0
    val uid = App.prefs.getPref("UID", "")
    private lateinit var listener: FinishListener

    fun start() {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setCancelable(false)
        dlg.setContentView(R.layout.region_dialog)

        dlg.show()

        dlg.region_cancel_btn.setOnClickListener {
            when (flag) {
                0 -> {
                    dismiss()
                }
                1 -> {
                    flag = 0
                    val adapter = RegionAdapter(regions) {
                        dlg.selected_text.text = it
                    }
                    dlg.region_recycler_view.layoutManager =
                        LinearLayoutManager(dlg.context, RecyclerView.VERTICAL, false)
                    dlg.region_recycler_view.adapter = adapter
                    dlg.selected_text_2.text = ""
                }
            }
        }

        dlg.region_select_btn.setOnClickListener {
            when (flag) {
                0 -> {
                    if (dlg.selected_text.text.toString() == "") Toast.makeText(
                        dlg.context,
                        "지역을 선택해 주세요",
                        Toast.LENGTH_SHORT
                    ).show()
                    else {
                        flag = 1
                        area = arrayListOf<String>("전체")
                        getArea()
                    }
                }
                1 -> {
                    if (dlg.selected_text_2.text.toString() == "") Toast.makeText(
                        dlg.context,
                        "구역을 선택해 주세요",
                        Toast.LENGTH_SHORT
                    ).show()
                    else {
                        when (DLG_TYPE) {
                            0 -> {
                                fs.collection("users").document(uid).update(
                                    hashMapOf(
                                        "region" to dlg.selected_text.text.toString(),
                                        "area" to dlg.selected_text_2.text.toString()
                                    ) as Map<String, String>
                                )
                                Toast.makeText(dlg.context, "지역이 변경됐습니다.", Toast.LENGTH_SHORT)
                                    .show()
                                dismiss()
                            }
                            1 -> {
                                Log.d("region", dlg.selected_text.text.toString())
                                Log.d("area", dlg.selected_text_2.text.toString())
                                listener.onClicked(
                                    TagInfo(
                                        dlg.selected_text.text.toString(),
                                        dlg.selected_text_2.text.toString()
                                    )
                                )
                                dismiss()
                            }
                        }

                    }
                }
            }
        }

        getRegions()
    }

    fun finish(listener: (TagInfo) -> Unit) {
        this.listener = object : FinishListener {
            override fun onClicked(content: TagInfo) {
                listener(content)
            }
        }
    }

    interface FinishListener {
        fun onClicked(content: TagInfo)
    }

    private fun dismiss() {
        dlg.dismiss()
    }

    private fun getRegions() {
        fs.collection("regions").orderBy("name").get().addOnSuccessListener { q ->

            q.documents.forEach { s ->
                regions.add(s.get("name").toString())
            }

            val adapter = RegionAdapter(regions) {
                dlg.selected_text.text = it
            }
            dlg.region_recycler_view.layoutManager =
                LinearLayoutManager(dlg.context, RecyclerView.VERTICAL, false)
            dlg.region_recycler_view.adapter = adapter

        }
    }

    private fun getArea() {
        fs.collection("regions").whereEqualTo("name", dlg.selected_text.text.toString()).get()
            .addOnSuccessListener { q ->

                if (!q.isEmpty) {
                    q.documents.forEach {
                        area.addAll(it.get("area") as ArrayList<String>)
                    }
                }
                val adapter = RegionAdapter(area) {
                    dlg.selected_text_2.text = it
                }
                dlg.region_recycler_view.layoutManager =
                    LinearLayoutManager(dlg.context, RecyclerView.VERTICAL, false)
                dlg.region_recycler_view.adapter = adapter

            }
    }
}