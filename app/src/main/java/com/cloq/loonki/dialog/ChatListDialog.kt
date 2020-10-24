package com.cloq.loonki.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.TextView
import com.cloq.loonki.App
import com.cloq.loonki.R
import com.cloq.loonki.fs

class ChatListDialog (context: Context) {
    private val dlg =Dialog(context)
    private lateinit var title: TextView
    private lateinit var alertConfig: TextView


    fun start(content: String, id: String , alert: Boolean) {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.chat_list_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴

        title = dlg.findViewById(R.id.dialog_content)
        title.text = content

        alertConfig = dlg.findViewById(R.id.dialog_chat_alert)
        when (alert) {
            true -> alertConfig.text = "채팅방 알림 끄기"
            false -> alertConfig.text = "채팅방 알림 켜기"
        }
        alertConfig.setOnClickListener {
            fs.collection("users").document(App.prefs.getPref("UID", "")).collection("rooms").document(id).update(
                hashMapOf(
                    "alert" to !alert
                ) as Map<String, Boolean>
            )
            dlg.dismiss()
        }


        alertConfig.setOnClickListener{
            fs.collection("users").document(App.prefs.getPref("UID", "")).collection("rooms").document(id).update(
                hashMapOf(
                    "alert" to alert.xor(true)
                ) as Map<String, Any>
            )
            dlg.dismiss()
        }

        dlg.show()
    }
}