package com.cloq.loonki

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.adapter.RegionTagCardAdapter
import com.cloq.loonki.data.ChatRoom
import com.cloq.loonki.data.UserRoom
import com.cloq.loonki.http.Http
import com.example.awesomedialog.*
import com.google.firebase.firestore.FieldValue
import kotlinx.android.synthetic.main.activity_random_channel.*
import java.sql.Timestamp
import kotlin.collections.ArrayList
import kotlin.random.Random

val matchedList = arrayListOf<String>()
val tags: ArrayList<TagInfo> = arrayListOf()

class RandomChannelActivity : AppCompatActivity() {
    private var type: Int = ChatRoom.ROOM_WHITE
    val uid = App.prefs.getPref("UID", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_channel)
        channel()

        tags.add(TagInfo("전체", "전체"))

        random_match_gender_any.isChecked = true

        start_region_tag_activity_link.setOnClickListener {
            val intent = Intent(this, RegionTagActivity::class.java)
            startActivity(intent)
        }

        getMatchedList()

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
                ) { startChatting() }
                .onNegative(
                    "취소",
                    buttonBackgroundColor = R.drawable.dialog_btn_bg,
                    textColor = Color.parseColor("#1A253F")
                ) { Log.d("TAG", "negative ") }
        }
    }

    override fun onStart() {
        super.onStart()
        addRegionCards()
    }

    override fun onDestroy() {
        super.onDestroy()
        tags.removeAll(tags)
    }

    private fun startChatting() {
        matchedList.removeAll(matchedList)
        if (matched_user_config.isChecked) getMatchedList()
        getUserList(gender(), range_bar.leftPinValue, range_bar.rightPinValue)
    }

    private fun gender(): String {
        return when {
            (random_match_gender_f.isChecked) -> "여자"
            (random_match_gender_m.isChecked) -> "남자"
            else -> ""
        }
    }

    private fun channel() {
        random_channel_nav.onItemSelected = {
            type = when (it) {
                0 -> ChatRoom.ROOM_WHITE
                1 -> ChatRoom.ROOM_BLACK
                else -> ChatRoom.ROOM_WHITE
            }
        }
    }

    private fun getMatchedList() {
        val userRef = fs.collection("users").document(uid)
        userRef.get().addOnSuccessListener { snapshot ->
            snapshot.get("user_random")
            if (snapshot.get("user_random") != null) {
                matchedList.addAll(snapshot.get("user_random") as ArrayList<String>)
            }
        }

    }

    private fun getUserList(gender: String, ageStart: String, ageEnd: String) {
        var userRef = fs.collection("users")
            .whereGreaterThan("age", ageStart.toInt())
            .whereLessThan("age", ageEnd.toInt())

        if (gender != "") userRef = userRef.whereEqualTo("gender", gender)

        val qal = arrayListOf<String>()
        var cnt = 0

        tags.forEach {
            when {
                (it.region == "전체") -> {
                    userRef.get().addOnSuccessListener { qs ->
                        println(qs.documents)
                        qs.documents.forEach { ds ->
                            qal.add(ds.id)
                        }
                        cnt++
                        if (cnt == tags.size) filterList(qal)
                    }
                }
                (it.area == "전체") -> {
                    userRef.whereEqualTo("region", it.region)
                        .get().addOnSuccessListener { qs ->
                            println(qs.documents)
                            qs.documents.forEach { ds ->
                                qal.add(ds.id)
                            }
                            cnt++
                            if (cnt == tags.size) filterList(qal)
                        }
                }
                else -> {
                    userRef.whereEqualTo("region", it.region).whereEqualTo("area", it.area)
                        .get().addOnSuccessListener { qs ->
                            println(it.region)
                            println(it.area)
                            println(qs.isEmpty)
                            qs.documents.forEach { ds ->
                                qal.add(ds.id)
                            }
                            cnt++
                            if (cnt == tags.size) filterList(qal)
                        }
                }
            }
        }
    }

    private fun filterList(list: ArrayList<String>) {
        println(list)
        val uid = App.prefs.getPref("UID", "")
        if (list.contains(uid)) list.remove(uid)
        if (matched_user_config.isChecked) list.removeAll(matchedList)
        if (list.size != 0) {
            val guest = pickUser(list)
            makeChat(guest, type)
        } else Toast.makeText(this, "조건에 맞는 유저가 없습니다...", Toast.LENGTH_SHORT).show()
    }

    private fun pickUser(list: ArrayList<String>): String {
        val n = list.size
        val idx = Random.nextInt(n)

        return list[idx]
    }

    private fun makeChat(guest: String, type: Int) {
        val users = arrayListOf(uid, guest)
        val room = ChatRoom(type, users, true)

        val time = System.currentTimeMillis()
        val roomMe = UserRoom(type, time, arrayListOf(guest), true, true)
        val roomYou = UserRoom(type, time, arrayListOf(uid), true, true)

        val roomRef = fs.collection("rooms")
        val userRef = fs.collection("users").document(uid)
        val guestRef = fs.collection("users").document(guest)

        roomRef.add(room).addOnSuccessListener {
            userRef.collection("rooms").document(it.id).set(roomMe)
            guestRef.collection("rooms").document(it.id).set(roomYou)
            userRef.update("user_random", FieldValue.arrayUnion(guest))

            checkTokenMatch(guest)
        }

    }

    private fun checkTokenMatch(uid: String) {
        fs.collection("users").document(uid).get().addOnSuccessListener { t ->
            val token = t.get("token").toString()
            fs.collection("tokens").document(token).get().addOnSuccessListener { u ->
                if (uid == u.get("uid").toString()) {
                    Http().randomMatchRequest(
                        token,
                        "",
                        "새로운 메세지 요청이 있습니다.",
                        uid,
                        type
                    )
                }
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
                        println("시작!")
                        val intent = Intent(this, MainActivity::class.java)

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                        intent.putExtra("position", 2)
                        startActivity(intent)
                    }
            }
        }
    }

    private fun addRegionCards() {
        val adapter = RegionTagCardAdapter(tags, false) {}
        random_region_cards_recycler.layoutManager =
            GridLayoutManager(this, 4, RecyclerView.HORIZONTAL, false)
        random_region_cards_recycler.adapter = adapter
    }
}
