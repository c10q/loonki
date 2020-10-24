package com.cloq.loonki

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.cloq.loonki.adapter.FriendTabAdapter
import com.cloq.loonki.fragment.FriendListFragment
import kotlinx.android.synthetic.main.activity_friend.*

class FriendActivity : AppCompatActivity() {
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_friend)
        setSupportActionBar(toolbar)
        toolbar_friend.navigationIcon?.setTint(getColor(R.color.color2))

        uid = App.prefs.getPref("UID", "")
        if (uid == "") return

        val friendFragment = FriendListFragment.newInstance("friend")
        val friendNewFragment = FriendListFragment.newInstance("new")

        val friendAdapter = FriendTabAdapter(supportFragmentManager)
        friendAdapter.addItems(friendFragment)
        friendAdapter.addItems(friendNewFragment)

        friend_view_page.adapter = friendAdapter
        friend_tab.setupWithViewPager(friend_view_page)

        friend_tab.getTabAt(0)?.text = "친구 목록"
        friend_tab.getTabAt(1)?.text = "새 요청"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    companion object FriendStatus {
        const val NOT_FRIEND = 0
        const val REQUEST_SENT = 1
        const val REQUEST_ACCEPT = 2
        const val CONNECTED = 3
        const val ME = 4
    }
}