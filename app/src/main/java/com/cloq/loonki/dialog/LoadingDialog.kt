package com.cloq.loonki.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.cloq.loonki.R

class LoadingDialog (context: Context) {
    private val dlg = Dialog(context)

    fun start() {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setCancelable(false)
        dlg.setContentView(R.layout.loading_dialog)

        dlg.show()
    }

    fun dismiss() {
        dlg.dismiss()
    }
}