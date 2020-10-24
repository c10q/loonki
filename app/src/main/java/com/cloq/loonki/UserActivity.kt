package com.cloq.loonki

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_user.*
import com.cloq.loonki.FriendActivity.FriendStatus
import com.cloq.loonki.data.ChatRoom
import com.cloq.loonki.data.UserRoom
import com.cloq.loonki.dialog.FriendDialog
import com.google.firebase.firestore.ListenerRegistration

class UserActivity : AppCompatActivity() {
    private lateinit var uid: String
    private lateinit var myUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_user)
        setSupportActionBar(toolbar)
        toolbar.navigationIcon?.setTint(getColor(R.color.color2))
        toolbar.overflowIcon?.setTint(getColor(R.color.color2))

        uid = intent.getStringExtra("uid").toString()
        myUid = App.prefs.getPref("UID", "")
        val userRef = fs.collection("users").document(uid)
        userRef.get().addOnSuccessListener {
            toolbar.subtitle = it["name"].toString()
            user_info_name_text.text = it["name"].toString()
            user_info_profile_img.setImageDrawable(null)
            GlideApp.with(this)
                .load(it["profile_url"])
                .override(256, 256)
                .circleCrop().into(user_info_profile_img)
        }
        if (uid == myUid) {
            setIcon(FriendStatus.ME)
            user_info_friend_btn.setOnClickListener {
                val intent = Intent(this, FriendActivity::class.java)
                startActivity(intent)
            }
        } else {
            userRef.collection("friends").document(myUid).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "error", e)
                    setIcon(FriendStatus.NOT_FRIEND)
                    return@addSnapshotListener
                }

                if (snapshot?.get("status") != null) {
                    setIcon((snapshot["status"] as Long).toInt())
                } else {
                    setIcon(FriendStatus.NOT_FRIEND)
                }
            }
            user_info_message_btn.setOnClickListener {
                fs.collection("users").document(myUid).collection("rooms")
                    .whereEqualTo("type", ChatRoom.ROOM_NORMAL).whereArrayContains("users", uid)
                    .get()
                    .addOnSuccessListener { room ->
                        if (!room.isEmpty) {
                            startChat(
                                room.documents[0].id,
                                uid,
                                arrayListOf(uid),
                                room.documents[0]["valid"] as Boolean
                            )
                        } else {
                            newRoom()
                        }
                    }
            }
        }
    }

    private fun newRoom() {
        fs.collection("rooms").add(
            ChatRoom(
                ChatRoom.ROOM_NORMAL,
                arrayListOf(uid, myUid),
                false
            )
        ).addOnSuccessListener {
            fs.collection("users").document(myUid)
                .collection("rooms").document(it.id).set(
                    UserRoom(
                        ChatRoom.ROOM_NORMAL,
                        System.currentTimeMillis(),
                        arrayListOf(uid),
                        true
                    )
                )
            fs.collection("users").document(uid)
                .collection("rooms").document(it.id).set(
                    UserRoom(
                        ChatRoom.ROOM_NORMAL,
                        System.currentTimeMillis(),
                        arrayListOf(myUid),
                        true
                    )
                )
            startChat(
                it.id,
                uid,
                arrayListOf(uid),
                false
            )
        }
    }

    private fun startChat(rid: String, roomName: String, users: ArrayList<String>, valid: Boolean) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("id", rid)
        intent.putExtra("roomName", roomName)
        intent.putExtra("users", users)
        intent.putExtra("type", ChatRoom.ROOM_NORMAL)
        intent.putExtra("valid", valid)
        startActivity(intent)
    }

    @SuppressLint("ResourceType")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_user, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_user_report -> {
                Toast.makeText(this, "신고!", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_user_hide -> {
                Toast.makeText(this, "숨기기!", Toast.LENGTH_SHORT).show()
            }
            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendRequest() {
        val ref = fs.collection("users").document(uid).collection("friends").document(myUid)
        val myRef = fs.collection("users").document(myUid).collection("friends").document(uid)
        ref.set(
            hashMapOf(
                "status" to FriendStatus.REQUEST_SENT
            )
        )
        myRef.set(
            hashMapOf(
                "status" to FriendStatus.REQUEST_ACCEPT
            )
        )
    }

    private fun acceptRequest(status: Int) {
        val ref = fs.collection("users").document(uid).collection("friends").document(myUid)
        val myRef = fs.collection("users").document(myUid).collection("friends").document(uid)
        ref.update(
            hashMapOf(
                "status" to status
            ) as Map<String, Any>
        )
        myRef.update(
            hashMapOf(
                "status" to status
            ) as Map<String, Any>
        )
    }

    private fun setIcon(status: Int) {
        when (status) {
            FriendStatus.NOT_FRIEND -> {
                user_info_friend_btn.setImageResource(R.drawable.ic_person_add_black_36dp)
                user_info_friend_sent.visibility = View.GONE
                user_info_friend_btn.setOnClickListener {
                    val dlg = FriendDialog(this)
                    dlg.start(uid, FriendStatus.NOT_FRIEND)
                    dlg.finish {
                        if (it) sendRequest()
                    }
                }
            }
            FriendStatus.REQUEST_SENT -> {
                user_info_friend_btn.setImageResource(R.drawable.ic_done_black_36dp)
                user_info_friend_sent.text = "전송됨"
                user_info_friend_sent.visibility = View.VISIBLE
                user_info_friend_btn.setOnClickListener {
                    val dlg = FriendDialog(this)
                    dlg.start(uid, FriendStatus.REQUEST_SENT)
                    dlg.finish {
                        if (it) acceptRequest(FriendStatus.NOT_FRIEND)

                    }
                }
            }
            FriendStatus.REQUEST_ACCEPT -> {
                user_info_friend_btn.setImageResource(R.drawable.ic_mark_email_unread_black_36dp)
                user_info_friend_sent.text = "수락/거절"
                user_info_friend_sent.visibility = View.VISIBLE
                user_info_friend_btn.setOnClickListener {
                    //acceptRequest()
                    val dlg = FriendDialog(this)
                    dlg.start(uid, FriendStatus.REQUEST_ACCEPT)
                    dlg.finish {
                        if (it) acceptRequest(FriendStatus.CONNECTED)
                        else acceptRequest(FriendStatus.NOT_FRIEND)
                    }
                }
            }
            FriendStatus.CONNECTED -> {
                user_info_friend_btn.setImageResource(R.drawable.ic_how_to_reg_black_36dp)
                user_info_friend_sent.visibility = View.GONE
                user_info_friend_btn.setOnClickListener {
                    val dlg = FriendDialog(this)
                    dlg.start(uid, FriendStatus.CONNECTED)
                    dlg.finish {
                        if (it) acceptRequest(FriendStatus.NOT_FRIEND)
                    }
                }
            }
            FriendStatus.ME -> {
                user_info_friend_btn.setImageResource(R.drawable.ic_person_black_48dp)
                user_info_friend_sent.visibility = View.GONE
                user_info_friend_btn.setOnClickListener {
                    val intent = Intent(this, FriendActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        user_info_loading.visibility = View.GONE
        user_info_profile_layout.visibility = View.VISIBLE
    }


}