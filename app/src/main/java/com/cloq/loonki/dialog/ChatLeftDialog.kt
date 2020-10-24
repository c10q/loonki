package com.cloq.loonki.dialog

import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.view.Window
import androidx.core.content.ContextCompat
import com.cloq.loonki.R
import kotlinx.android.synthetic.main.chat_left_dialog.*

class ChatLeftDialog (context: Context) {
    private val dlg = Dialog(context)

    fun start(message: String) {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(R.layout.chat_left_dialog)     //다이얼로그에 사용할 xml 파일을 불러옴

        dlg.dialog_send_left_copy.setOnClickListener {

        }
        dlg.show()
    }
}