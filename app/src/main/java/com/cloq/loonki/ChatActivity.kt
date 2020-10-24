package com.cloq.loonki

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.adapter.ChatAdapter
import com.cloq.loonki.data.ChatRoom
import com.cloq.loonki.http.Http
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_chat.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class ChatActivity() : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private lateinit var id: String
    private lateinit var users: ArrayList<String>
    private var roomType by Delegates.notNull<Int>()

    private lateinit var roomRef: CollectionReference
    private lateinit var lastVisible: DocumentSnapshot
    private lateinit var startAt: DocumentSnapshot

    private val uid = App.prefs.getPref("UID", "")
    private val chats = mutableListOf<Model>()
    private var itemCnt = 0
    private var loading = false

    private var valid by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_chat)
        setSupportActionBar(toolbar)
        toolbar.navigationIcon?.setTint(getColor(R.color.color2))
        toolbar.menu[0].icon.setTint(getColor(R.color.color2))

        id = intent.getStringExtra("id").toString()
        users = intent.getStringArrayListExtra("users") as ArrayList<String>
        roomType = intent.getIntExtra("type", 99)

        when (roomType) {
            ChatRoom.ROOM_NORMAL -> {
                fs.collection("users").document(users[0]).get().addOnSuccessListener {
                    toolbar.title = it["name"].toString()
                }
            }
            else -> {
                toolbar.title = intent.getStringExtra("roomName")
            }
        }

        roomRef = fs.collection("rooms").document(id).collection("chats")

        valid = intent.getBooleanExtra("valid", false)
        println(valid)

        if (valid) {
            firstLoad()
        }

        send_btn.setOnClickListener {
            val message = post_comment_edit_text.text.toString()
            sendMessage(message)
            post_comment_edit_text.text.clear()
        }
    }

    @SuppressLint("ResourceType")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_chat_setting -> {
                val intent = Intent(this, ChatInfoActivity::class.java)
                intent.putExtra("id", id)
                if (users.size == 1) intent.putExtra("user", users[0])
                intent.putExtra("type", roomType)
                intent.putExtra("name", toolbar_chat.title.toString())
                startActivity(intent)
            }
            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setListener() {
        println(users)
        roomRef.whereGreaterThan("time", System.currentTimeMillis())
            .whereEqualTo("sender", users[0])
            .orderBy("time", Query.Direction.DESCENDING).endBefore(startAt).limit(1)
            .addSnapshotListener { value, _ ->
                println("all")
                value?.documents?.forEach {
                    val sender = it["sender"]
                    println("me")

                    if (!(it["read"] as Boolean))
                        roomRef.document(it.id)
                            .update(hashMapOf("read" to true) as Map<String, Any>)
                    else {
                        val stamp = it.get("time") as Long
                        val message = it["message"]
                        val view: Int = Model.LEFT_VIEW
                        val chat = Model(view, message.toString(), stamp, it.id)

                        if (!compareDate(stamp, chats[0].stamp)) chats.add(
                            0,
                            Model(Model.CENTER_VIEW, "", stamp)
                        )
                        chats.add(0, chat)
                        adapter.notifyItemChanged(0)
                        adapter.notifyItemInserted(0)

                        val r = chat_room_recycler_view
                        val pView = chat_preview_text_card
                        val pText = chat_preview_text
                        when {
                            (r.getChildAdapterPosition(r.getChildAt(0)) < 2) -> r.scrollToPosition(
                                0
                            )
                            else -> {
                                pView.visibility = View.VISIBLE
                                pText.text = message.toString()
                                pView.setOnClickListener {
                                    r.scrollToPosition(0)
                                    pView.visibility = View.GONE
                                }
                            }
                        }
                    }

                }
            }
    }

    private fun deleteListener(now: Long) {
        fs.collection("rooms").document(id).collection("deleted")
            .whereGreaterThan("deletedAt", now)
            .orderBy("deletedAt", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { v, e ->
                if (v != null && v.documents.size != 0 && !loading)
                    chats.forEachIndexed { i, it ->
                        if (it.chatID == v.documents[0].id) {
                            it.deleted = true
                            adapter.notifyItemChanged(i)
                        }
                    }
            }
    }

    private fun firstLoad() {
        roomRef.orderBy("time", Query.Direction.DESCENDING).limit(25).get()
            .addOnSuccessListener { v ->
                if (v.isEmpty) {
                    return@addOnSuccessListener
                }
                loading = true
                getData(v)
                adapter = ChatAdapter(chats, roomType, id, users[0])
                chat_room_recycler_view.layoutManager =
                    LinearLayoutManager(this, RecyclerView.VERTICAL, true)
                chat_room_recycler_view.adapter = adapter
                setListener()
                deleteListener(System.currentTimeMillis())
                val sectionItemDecoration = RecyclerSectionItemDecoration(getSectionCallback())
                chat_room_recycler_view.addItemDecoration(sectionItemDecoration)
                chat_room_recycler_view.addOnScrollListener(object :
                    RecyclerView.OnScrollListener() {
                    override fun onScrolled(r: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(r, dx, dy)
                        if (r.getChildAdapterPosition(r.getChildAt(0)) < 1) {
                            chat_preview_text_card.visibility = View.GONE
                        }
                        if (r.getChildAdapterPosition(r.getChildAt(r.childCount - 1)) > (itemCnt - 10) && !loading) {
                            loading = true
                            roomRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(25)
                                .startAfter(lastVisible).get()
                                .addOnSuccessListener { qsChild ->
                                    if (!qsChild.isEmpty) {
                                        chats.removeAt(chats.size - 1)
                                        getData(qsChild)
                                        adapter.notifyItemChanged(0)
                                    } else {
                                        chats.removeAt(chats.size - 1)
                                        chats.add(
                                            Model(
                                                Model.CENTER_VIEW,
                                                "",
                                                chats[chats.size - 1].stamp
                                            )
                                        )
                                        adapter.notifyItemChanged(0)
                                        loading = false
                                    }
                                }
                        }
                    }
                })
            }
    }

    private fun getData(qs: QuerySnapshot) {
        var lastStamp: Long = 0

        startAt = qs.documents[0]
        lastVisible = qs.documents[qs.documents.size - 1]

        qs.documents.forEachIndexed() { i, it ->
            itemCnt++
            val stamp = it["time"] as Long
            if (!compareDate(stamp, lastStamp) && i != 0) {
                chats.add(Model(Model.CENTER_VIEW, "", lastStamp, ""))
                itemCnt++
            }
            lastStamp = stamp
            val sender = it["sender"].toString()
            val view: Int
            val message: String = it["message"].toString()
            val deleted = if (it["deleted"] != null)
                it["deleted"] as Boolean
            else false

            if (sender == uid) view = Model.RIGHT_VIEW
            else {
                view = Model.LEFT_VIEW
                if (!(it["read"] as Boolean)) roomRef.document(it.id).update(mapOf("read" to true))
            }

            chats.add(Model(view, message, stamp, it.id, deleted))
        }
        if (qs.documents.size < 25)
            chats.add(Model(Model.CENTER_VIEW, "", lastStamp))
        else
            chats.add(Model(Model.LOADING_VIEW, "", lastStamp))

        loading = false
    }

    private fun getSectionCallback(): RecyclerSectionItemDecoration.SectionCallback {
        return object : RecyclerSectionItemDecoration.SectionCallback {
            override fun isSection(position: Int): Boolean {
                return adapter.isHolder(position)
            }

            override fun getHeaderLayoutView(list: RecyclerView, position: Int): View? {
                return adapter.getHeaderLayoutView(list, position)
            }
        }
    }

    private fun sendMessage(ms: String) {
        val time = System.currentTimeMillis()
        roomRef.add(
            hashMapOf(
                "message" to ms,
                "sender" to uid,
                "timestamp" to Timestamp(time),
                "time" to time,
                "read" to false
            ) as Map<String, Any>
        ).addOnSuccessListener {
            if (!valid) {
                firstLoad()
                fs.collection("rooms").document(id).update(
                    hashMapOf("valid" to true) as Map<String, Any>
                ).addOnSuccessListener {
                    fs.collection("users").document(uid).collection("rooms").document(id).update(
                        hashMapOf("valid" to true) as Map<String, Any>
                    )
                    fs.collection("users").document(users[0]).collection("rooms").document(id)
                        .update(
                            hashMapOf("valid" to true) as Map<String, Any>
                        )
                    valid = true
                }
            } else {
                if (!compareDate(chats[0].stamp, time)) chats.add(
                    0,
                    Model(Model.CENTER_VIEW, "", time)
                )
                chats.add(0, Model(Model.RIGHT_VIEW, ms, time, it.id))
                adapter.notifyItemInserted(0)
                chat_room_recycler_view.scrollToPosition(0)
            }
        }

        val timeUpdate = hashMapOf(
            "time" to time
        ) as Map<String, Long>

        fs.collection("users").document(uid).collection("rooms").document(id).update(timeUpdate)
        users.forEach {
            fs.collection("users").document(it).collection("rooms").document(id).update(timeUpdate)
        }

        if (users.size != 0) {
            users.forEach { sendID ->
                fs.collection("users").document(sendID).get()
                    .addOnSuccessListener { documentSnapshot ->
                        documentSnapshot.get("token")
                        Http().chatSendRequest(
                            documentSnapshot.get("token").toString(),
                            uid,
                            ms,
                            sendID,
                            "chat",
                            id
                        )
                    }
            }
        }
        post_comment_edit_text.setText("")
    }

    @SuppressLint("SimpleDateFormat")
    private fun compareDate(d1: Long, d2: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd")
        return sdf.format(d1) == sdf.format(d2)
    }
}

data class Model(
    val type: Int,
    val message: String?,
    val stamp: Long,
    val chatID: String = "",
    var deleted: Boolean = false
) {
    companion object {
        const val CENTER_VIEW = 0
        const val LEFT_VIEW = 1
        const val RIGHT_VIEW = 2
        const val LOADING_VIEW = 99
    }
}