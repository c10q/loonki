package com.cloq.loonki

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloq.loonki.Http.Http
import com.example.awesomedialog.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_random_channel.*
import kotlin.collections.ArrayList
import kotlin.random.Random

var filteredList = arrayListOf<String>()
var onlineList = arrayListOf<String>()
var curList = arrayListOf<String>()
var resultList = arrayListOf<String>()
var cnt: Int = 0
var type: String = "white"

val CHANNEL_ID: String = "create_message"

class RandomChannelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_channel)

        random_channel_nav.onItemSelected = {
            when (it) {
                0 -> {
                    type = "WHITE"
                }
                1 -> {
                    type = "BLACK"
                }
            }
        }

        range_bar.tickStart = 10F
        range_bar.tickEnd = 50F

        button.setOnClickListener {
            AwesomeDialog.build(this)
                .title("시작할까요?", titleColor = Color.parseColor("#EDD7C3"))
                .body("white는 엄격한 기준을 적용합니다.", color = Color.parseColor("#EDD7C3"))
                .background(R.drawable.dialog_bg)
                .onPositive(
                    "시작 하기!",
                    buttonBackgroundColor = R.drawable.dialog_btn_bg,
                    textColor = Color.parseColor("#1A253F")
                ) {
                    startChatting()
                }
                .onNegative(
                    "취소",
                    buttonBackgroundColor = R.drawable.dialog_btn_bg,
                    textColor = Color.parseColor("#1A253F")
                ) {
                    Log.d("TAG", "negative ")
                }
        }
    }

    private fun startChatting() {
        filteredList = arrayListOf<String>()
        onlineList = arrayListOf<String>()
        curList = arrayListOf<String>()
        resultList = arrayListOf<String>()
        cnt = 0

        getOnlineList()
        getUserList(gender(), region(), range_bar.leftPinValue, range_bar.rightPinValue)
    }

    private fun gender(): String {
        return if (random_match_gender_f.isChecked) "여자"
        else if (random_match_gender_m.isChecked) "남자"
        else if (random_match_gender_any.isChecked) "무작위"
        else "모르겠어"
    }

    private fun region(): String {
        return if (random_match_region_same.isChecked) "서울"
        else if (random_match_region_any.isChecked) "무작위"
        else "모르겠어"
    }

    private fun getUserList(gender: String, region: String, ageStart: String, ageEnd: String) {
        db.getReference("users")
            .orderByChild("age")
            .startAt(ageStart)
            .endAt(ageEnd)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount.toInt() != 0) {
                        cnt = snapshot.childrenCount.toInt()
                        if (gender == "무작위") {
                            if (region == "무작위") {
                                snapshot.children.forEach {
                                    filteredList.add(it.ref.key.toString())
                                    if (filteredList.size == cnt) findInOnline()
                                }
                            } else {
                                filterRegion(snapshot, region)
                            }
                        } else {
                            filterGender(snapshot, gender, region)
                        }
                    } else {
                        Toast.makeText(this@RandomChannelActivity, "없어 없어..", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })
    }

    private fun filterGender(users: DataSnapshot, gender: String, region: String) {
        users.children.forEach { u ->
            db.getReference("user_gender")
                .child(gender)
                .orderByChild("uid")
                .equalTo(u.ref.key.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (region == "무작위") {
                            filteredList.add(u.ref.key.toString())
                            if (filteredList.size == cnt) findInOnline()
                        } else {
                            if (snapshot.childrenCount.toInt() == 1) filterRegion(snapshot, region)
                            else cnt--
                        }

                        if (cnt == 0) {
                            Toast.makeText(
                                this@RandomChannelActivity,
                                "없어 없어..",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        }
    }

    private fun filterRegion(gender: DataSnapshot, region: String) {
        gender.children.forEach { g ->
            db.getReference("user_region")
                .child(region)
                .orderByChild("uid")
                .equalTo(g.ref.key.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.childrenCount.toInt() == 1) {
                            filteredList.add(g.ref.key.toString())
                        } else cnt--

                        if (filteredList.size == cnt) findInOnline()

                    }
                })
        }
    }

    fun findInOnline() {
        if (cnt == 0) Toast.makeText(this, "지역을 변경해주세요", Toast.LENGTH_SHORT).show()
        Log.d("online_filteredList", filteredList.toString())
        Log.d("online_onlineList", onlineList.toString())

        for (i in filteredList) {
            for (j in onlineList) {
                if (i == j && i != auth?.uid.toString()) {
                    resultList.add(i)
                }
            }
        }

        println(resultList.size)

        if (resultList.size == 0) {
            val start: String = (System.currentTimeMillis() - 70000).toString()
            val end: String = (System.currentTimeMillis() + 60).toString()
            getCurList(start, end)
        } else {
            makeChat(pickUser(resultList), type)
        }
    }

    private fun findInCurrent() {
        for (i in filteredList) {
            for (j in curList) {
                if (i == j && i != auth?.uid.toString()) {
                    resultList.add(i)
                }
            }
        }
        if (resultList.size != 0) Toast.makeText(this, pickUser(resultList), Toast.LENGTH_SHORT)
            .show()
        else makeChat(pickUser(filteredList), type)
    }

    private fun pickUser(list: ArrayList<String>): String {
        val n = list.size
        val idx = Random.nextInt(n)
        Log.d("이 배열 안에서", list.toString())
        Log.d("선택된 유저", list[idx])

        return list[idx]
    }

    private fun makeChat(guest: String, type: String) {
        val cRef = db.getReference("chat_room")
        val key = cRef.push().key

        data class ChatRoom(
            val host: String,
            val guest: String,
            val type: String
        ) {
            fun toMap(): Map<String, Any?> {
                return mapOf(
                    "host" to host,
                    "guest" to guest,
                    "type" to type
                )
            }
        }

        val cValue = ChatRoom(auth?.uid.toString(), guest, type).toMap()

        val chatRoomUpdate = hashMapOf<String, Any>(
            "/chat_room/$key" to cValue,
            "/users/${auth?.uid}/chat_room/$key/" to cValue
        )

        db.reference.updateChildren(chatRoomUpdate)

        checkTokenMatch(guest)

        AwesomeDialog.build(this)
            .icon(R.drawable.ic_congrts)
            .title("채팅 요청을 보냈습니다.", titleColor = Color.parseColor("#EDD7C3"))
            .body("상대방이 수락하면 대화가 시작됩니다!", color = Color.parseColor("#EDD7C3"))
            .background(R.drawable.dialog_bg)
            .onPositive(
                "확인",
                buttonBackgroundColor = R.drawable.dialog_btn_bg,
                textColor = Color.parseColor("#1A253F")
            ) {
                val intent = Intent(this, MainActivity::class.java)

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)
            }

    }

    private fun getOnlineList() {
        db.getReference("online_user")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        onlineList.add(it.key.toString())
                    }
                }
            })
    }

    private fun getCurList(start: String, end: String) {
        db.getReference("users_connection")
            .orderByChild("last_login")
            .startAt(start)
            .endAt(end)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val cnt = snapshot.childrenCount.toInt()
                        curList.add(it.ref.key.toString())

                        if (cnt == curList.size) findInCurrent()
                    }
                }
            })
    }

    private fun checkTokenMatch(uid: String) {
        db.getReference("user_token").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val token = snapshot.value.toString()

                    db.getReference("token_user").child(token)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.value.toString() == uid) {
                                    Http().randomMatchRequest(
                                        token,
                                        type,
                                        "새로운 $type 메세지 요청이 있습니다.",
                                        uid
                                    )
                                }
                            }
                        })

                }

            })
    }

}