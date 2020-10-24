package com.cloq.loonki

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.cloq.loonki.data.ChatRoom
import com.cloq.loonki.dialog.ReportDialog
import com.example.awesomedialog.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.model.Document
import kotlinx.android.synthetic.main.activity_chat_info.*
import kotlinx.coroutines.internal.SynchronizedObject

class ChatInfoActivity : AppCompatActivity() {
    private lateinit var name: String
    private lateinit var myRoomRef: DocumentReference
    private lateinit var roomRef: DocumentReference
    private lateinit var uid: String
    private lateinit var user: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_info)

        name = intent.getStringExtra("name").toString()
        chat_info_room_title.setText(name)
        if (intent.getIntExtra("type", 99) == ChatRoom.ROOM_BLACK)
            chat_info_report_btn.visibility = View.GONE

        uid = App.prefs.getPref("UID", "")
        user = intent.getStringExtra("user").toString()

        val roomID = intent.getStringExtra("id").toString()
        val alertBox = chat_info_alert_checkbox

        chat_info_clear_btn.setOnClickListener {
            super.onBackPressed()
        }

        if (uid != "") {
            roomRef = fs.collection("rooms").document(roomID)
            myRoomRef = fs.collection("users").document(uid).collection("rooms").document(roomID)
            myRoomRef.get().addOnSuccessListener {
                println(it["alert"])
                alertBox.isChecked = it["alert"] as Boolean
            }

            alertBox.setOnClickListener {
                if (!alertBox.isChecked) myRoomRef.update(hashMapOf("alert" to false) as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "알림을 차단했습니다.", Toast.LENGTH_SHORT).show()
                    }
                else myRoomRef.update(hashMapOf("alert" to true) as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "알림을 허용했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }

            chat_info_room_name_btn.setOnClickListener {
                rename()
            }

            chat_info_room_exit_btn.setOnClickListener {
                disconnect()
            }

            chat_info_report_btn.setOnClickListener {
                val dlg = ReportDialog(this)
                dlg.start()
                dlg.finish { data ->
                    saveReport(data["reason"].toString(), uid, user)
                }
            }
        } else intentAuth()

    }


    private fun rename() {
        if (chat_info_room_title.text.toString() == name) Toast.makeText(
            this,
            "수정된 정보가 없습니다.",
            Toast.LENGTH_SHORT
        ).show()
        else {
            val nameChanged = chat_info_room_title.text.toString()
            myRoomRef.update(hashMapOf("room_name" to nameChanged) as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "이름이 변경됐습니다.", Toast.LENGTH_SHORT).show()
                    name = nameChanged
                }
        }
    }

    private fun disconnect() {
        AwesomeDialog.build(this)
            .body("연결을 종료할까요?", color = Color.parseColor("#EDD7C3"))
            .background(R.drawable.dialog_bg)
            .onPositive(
                "연결 끊기",
                buttonBackgroundColor = R.drawable.dialog_btn_bg,
                textColor = Color.parseColor("#1A253F")
            ) {
                submitDisconnect()
            }
            .onNegative(
                "취소",
                buttonBackgroundColor = R.drawable.dialog_btn_bg2,
                textColor = Color.parseColor("#1A253F")
            ) {}
    }

    private fun submitDisconnect() {
        val data = hashMapOf("deleted" to true) as Map<String, Any>
        myRoomRef.update(data).addOnSuccessListener {
            roomRef.update(data).addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("position", 2)

                startActivity(intent)
            }
        }

    }

    private fun saveReport(reason: String, uid: String, user: String) {
        AwesomeDialog.build(this)
            .title("주의!", titleColor = Color.parseColor("#EDD7C3"))
            .body(
                """Loonki Table에는 
                    |무지개 반사가 탑재돼있습니다.
                    |
                    |허위신고를 탐지하면 발동할지도...""".trimMargin(),
                color = Color.parseColor("#EDD7C3")
            )
            .background(R.drawable.dialog_bg)
            .onPositive(
                "신고하기",
                buttonBackgroundColor = R.drawable.dialog_btn_bg,
                textColor = Color.parseColor("#1A253F")
            ) {
                submitReport(reason, uid, user)
            }
            .onNegative(
                "취소",
                buttonBackgroundColor = R.drawable.dialog_btn_bg2,
                textColor = Color.parseColor("#1A253F")
            ) {}
    }

    private fun submitReport(reason: String, uid: String, user: String) {
        fs.collection("report").add(
            hashMapOf(
                "reason" to reason,
                "reporter" to uid,
                "reported_person" to user,
                "done" to false,
                "time" to System.currentTimeMillis()
            ) as Map<String, Any>
        ).addOnSuccessListener {
            Toast.makeText(this, "신고가 정상적으로 접수됐습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun intentAuth() {
        Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, AuthActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    }
}