package com.cloq.loonki.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.cloq.loonki.R
import com.cloq.loonki.fs
import kotlinx.android.synthetic.main.chat_right_dialog.*

class ChatRightDialog (context: Context) {
    private val dlg =Dialog(context)

    fun start(message: String, id: String, roomID: String) {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.chat_right_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴

        dlg.dialog_send_cancel.setOnClickListener {
            fs.collection("rooms").document(roomID).collection("chats").document(id).update(mapOf("deleted" to true))
            fs.collection("rooms").document(roomID).collection("deleted").document(id).set(mapOf("deletedAt" to System.currentTimeMillis()))
            dlg.dismiss()
        }

        dlg.dialog_send_right_copy.setOnClickListener {

        }

        dlg.show()
    }
}