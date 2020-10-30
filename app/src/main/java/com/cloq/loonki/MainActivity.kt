package com.cloq.loonki

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.viewpager.widget.ViewPager
import com.cloq.loonki.adapter.BottomNavAdapter
import com.cloq.loonki.fragment.*
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_square.*
import kotlinx.android.synthetic.main.bottom_navigation_tab.*

val fs = FirebaseFirestore.getInstance()
val db = FirebaseDatabase.getInstance()

class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    @SuppressLint("ResourceType", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        toolbar = findViewById(R.id.toolbar_main)
        toolbar.overflowIcon?.setTint(getColor(R.color.color2))
        toolbar.title = "HOME"
        replaceFragment(MyPageFragment(), "MY")
        toolbar.menu[0].icon.setTint(getColor(R.color.color2))
        toolbar.menu[0].setOnMenuItemClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
            true
        }

        bottom_nav.onItemSelected = {
            toolbar.menu.clear()
            when (it) {
                0 -> {
                    replaceFragment(MyPageFragment(), "MY")
                    toolbar.menu.add("").setIcon(getDrawable(R.drawable.ic_more_vert_24px))
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    toolbar.menu[0].icon.setTint(getColor(R.color.color2))
                    toolbar.menu[0].setOnMenuItemClickListener {
                        val intent = Intent(this, EditProfileActivity::class.java)
                        startActivity(intent)
                        true
                    }
                }
                1 -> {
                    replaceFragment(ChannelFragment(), "CHANNELS")
                }
                2 -> {
                    replaceFragment(HomeFragment(), "HOME")
                    toolbar.menu.add("home").setIcon(getDrawable(R.drawable.ic_add_24px))
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    toolbar.menu[0].icon.setTint(getColor(R.color.color2))
                    toolbar.menu[0].setOnMenuItemClickListener {
                        val intent = Intent(this, AddPostActivity::class.java)
                        startActivity(intent)
                        true
                    }
                }
                3 -> {
                    replaceFragment(ChatFragment(), "MESSAGES")
                }
                4 -> {
                    replaceFragment(SettingsFragment(), "SETTING")
                }
            }
        }

        db.goOnline()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        toolbar.title = title
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.bottom_nav_bg, fragment)
        fragmentTransaction.commit()
    }

    override fun onStart() {
        super.onStart()
        val user = App.prefs.getPref("UID", "")
        val token = App.prefs.getPref("FCM_Token", "")
        if (user == "" && token == "") {
            intentAuth()
        }
    }

    private fun intentAuth() {
        val intent = Intent(this, AuthActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        db.goOffline()
    }

    data class Post(
        val type: Int,
        val time: String,
        val uid: String,
        val pid: String,
        val text: String,
        val image: Boolean = false
    ) {
        companion object {
            const val NOTICE_CONTENT = 0
            const val MAIN_CONTENT = 1
            const val LOADING = 2
        }
    }
}