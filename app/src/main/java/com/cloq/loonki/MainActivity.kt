package com.cloq.loonki

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.cloq.loonki.Adapter.BottomNavAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_home.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth?.currentUser?.reload()

        checkConnection()

        configureBottomNavigation()
    }

    override fun onStart() {
        super.onStart()
        if (auth?.currentUser == null) {
            val intent = Intent(this, AuthActivity::class.java)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
        } else {
            db.getReference("user_token").child(auth?.uid.toString())
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("토큰 확인용", snapshot.toString())
                        if (snapshot.value.toString() != FirebaseInstanceId.getInstance().token.toString()) {

                            auth?.signOut()
                            auth?.currentUser?.reload()

                            val intent = Intent(this@MainActivity, AuthActivity::class.java)

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                            startActivity(intent)
                        }
                    }

                })
        }
    }

    private fun checkConnection() {
        val uid = auth?.currentUser?.uid

        val connectedRef = db.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {

                    val onlineRef = db.getReference("online_user").child(uid.toString())
                    onlineRef.setValue(uid)
                    onlineRef.onDisconnect().removeValue()

                    val userConnectRef = db.getReference("users_connection").child(uid.toString())

                    val stateRef = userConnectRef.child("state")
                    stateRef.setValue("online")
                    stateRef.onDisconnect().setValue("disconnected")

                    val lastLoginRef = userConnectRef.child("last_login")
                    lastLoginRef.setValue(System.currentTimeMillis().toString())

                    val lastConnectRef = userConnectRef.child("last_connection")
                    lastConnectRef.onDisconnect().setValue(System.currentTimeMillis().toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
            val token = task.result?.token


        })
    }

    override fun onDestroy() {
        super.onDestroy()
        val userID = App.prefs.getPref("UID", "No User")
        println(userID)
    }

    fun getDateTime(stamp: Timestamp): String? {
        return try {
            val date = Date(stamp.time)
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA)
            sdf.format(date)
        } catch (e: Exception) {
            e.toString()
        }
    }

    @SuppressLint("InflateParams")
    private fun configureBottomNavigation() {
        bottom_nav_bg.adapter = BottomNavAdapter(supportFragmentManager, 4)

        bottom_nav.setupWithViewPager(bottom_nav_bg)

        val bottomNav: View =
            this.layoutInflater.inflate(R.layout.bottom_navigation_tab, null, false)

        bottom_nav.getTabAt(0)!!.customView =
            bottomNav.findViewById(R.id.btn_my_page_nav) as RelativeLayout
        bottom_nav.getTabAt(1)!!.customView =
            bottomNav.findViewById(R.id.btn_star_nav) as RelativeLayout
        bottom_nav.getTabAt(2)!!.customView =
            bottomNav.findViewById(R.id.btn_chat_nav) as RelativeLayout
        bottom_nav.getTabAt(3)!!.customView =
            bottomNav.findViewById(R.id.btn_settings_nav) as RelativeLayout
    }
}