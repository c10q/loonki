package com.cloq.loonki.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Window
import com.cloq.loonki.R
import com.cloq.loonki.TagInfo
import com.example.awesomedialog.*
import kotlinx.android.synthetic.main.report_dialog.*

class ReportDialog (context: Context) {
    private val dlg = Dialog(context)
    private lateinit var listener: FinishListener

    fun start() {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.report_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴

        dlg.report_radio_group.setOnCheckedChangeListener { radioGroup, i ->
            when(radioGroup.checkedRadioButtonId) {
                dlg.report_item_5.id -> {
                    dlg.report_resaon_edit_text.isEnabled = true
                }
                else -> {
                    dlg.report_resaon_edit_text.text.clear()
                    dlg.report_resaon_edit_text.isEnabled = false
                }
            }
        }

        dlg.report_submit_btn.setOnClickListener {
            listener.onClicked(
                hashMapOf(
                    "reason" to reasonForReporting()
                )
            )
            dlg.dismiss()
        }

        dlg.show()
    }

    fun finish(listener: (HashMap<String, Any>) -> Unit) {
        this.listener = object : FinishListener {
            override fun onClicked(content: HashMap<String, Any>) {
                listener(content)
            }
        }
    }

    interface FinishListener {
        fun onClicked(content: HashMap<String, Any>)
    }

    private fun reasonForReporting() {
        when {
            dlg.report_item_1.isChecked -> dlg.report_item_1.text
            dlg.report_item_2.isChecked -> dlg.report_item_2.text
            dlg.report_item_3.isChecked -> dlg.report_item_3.text
            dlg.report_item_4.isChecked -> dlg.report_item_4.text
            dlg.report_item_5.isChecked -> dlg.report_resaon_edit_text.text
        }
    }
}