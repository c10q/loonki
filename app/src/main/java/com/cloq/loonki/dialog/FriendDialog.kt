package com.cloq.loonki.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.cloq.loonki.FriendActivity.FriendStatus
import com.cloq.loonki.GlideApp
import com.cloq.loonki.R
import com.cloq.loonki.fs
import kotlinx.android.synthetic.main.dialog_friend.*

class FriendDialog(context: Context) {
    private val dlg = Dialog(context)
    private lateinit var listener: FriendDialogListener

    fun start(uid: String, type: Int) {
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.dialog_friend)

        when (type) {
            FriendStatus.REQUEST_ACCEPT -> {
                setText(
                    "친구 요청",
                    "수락하겠습니까?",
                    "수락하기",
                    "거절하기"
                )
            }
            FriendStatus.NOT_FRIEND -> {
                setText(
                    "친구 요청 전송",
                    "친구 요청을 보내겠습니까?",
                    "전송하기",
                    "취소"
                )
            }
            FriendStatus.REQUEST_SENT -> {
                setText(
                    "친구 요청 삭제",
                    "친구 요청을 삭제하겠습니까?",
                    "삭제하기",
                    "취소"
                )
            }
            FriendStatus.CONNECTED -> {
                setText(
                    "친구 삭제",
                    "친구를 삭제하겠습니까?",
                    "삭제하기",
                    "취소"
                )
            }
        }

        fs.collection("users").document(uid).get().addOnSuccessListener { user ->
            dlg.dialog_friend_name.text = user["name"].toString()
            GlideApp.with(dlg.context).load(
                user["profile_url"]
            ).circleCrop().into(dlg.dialog_friend_image)
        }

        dlg.dialog_friend_accept.setOnClickListener {
            listener.onClicked(true)
        }

        dlg.dialog_friend_reject.setOnClickListener {
            listener.onClicked(false)
        }

        dlg.show()
    }

    fun finish(listener: (Boolean) -> Unit) {
        this.listener = object : FriendDialogListener {
            override fun onClicked(content: Boolean) {
                listener(content)
                dlg.dismiss()
            }
        }
    }

    private fun setText(title: String, message: String, btn1: String, btn2: String) {
        dlg.dialog_friend_type.text = title
        dlg.dialog_friend_message.text = message
        dlg.dialog_friend_accept.text = btn1
        dlg.dialog_friend_reject.text = btn2
    }

    interface FriendDialogListener {
        fun onClicked(content: Boolean)
    }
}