package com.cloq.loonki

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.adapter.CommentAdapter
import com.cloq.loonki.fragment.storage
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_post.*
import java.text.SimpleDateFormat
import java.util.*

class PostActivity : AppCompatActivity() {
    private lateinit var pid: String
    private lateinit var uid: String
    private val commentList = arrayListOf<Comment>()
    private lateinit var adapter: CommentAdapter

    @SuppressLint("RestrictedApi", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_post)
        setSupportActionBar(toolbar)
        toolbar.navigationIcon?.setTint(getColor(R.color.color2))
        toolbar.overflowIcon?.setTint(getColor(R.color.color2))

        uid = App.prefs.getPref("UID", "")
        if (uid == "") {
            return
        }

        post_comment_submit_btn.setOnClickListener {
            submitComment()
        }

        pid = intent.getStringExtra("pid").toString()

        getData()
    }

    @SuppressLint("ResourceType")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_post, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_post_report -> {
                Toast.makeText(this, "신고!", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_post_remove -> {
                Toast.makeText(this, "삭제!", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_post_hide -> {
                Toast.makeText(this, "숨기기!", Toast.LENGTH_SHORT).show()
            }
            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getData() {
        fs.collection("posts").document(pid).get().addOnSuccessListener { post ->
            fs.collection("users").document(post["writer"].toString()).get()
                .addOnSuccessListener { user ->
                    post_user_image.setImageDrawable(null)
                    GlideApp.with(this).load(
                        user["profile_url"].toString()
                    ).circleCrop().into(post_user_image)
                    post_writer_text.text = user["name"].toString()
                    toolbar_post.subtitle = user["name"].toString()
                }
            if (post["image"] != null && post["image"] as Boolean) {
                post_image.visibility = View.VISIBLE
                post_image.setImageDrawable(null)
                //post_image.layoutParams.height = post_image.width
                GlideApp.with(this).load(
                    storage.getReference("posts/${post.id}/image.png")
                ).into(post_image)
            }
            post_text.text = post["text"].toString()
            post_like_count.text = post["like"].toString()
            val time = post["time"] as Long
            post_time.text = SimpleDateFormat("|  yyyy. MM. dd", Locale.KOREA).format(time)

        }
        fs.collection("posts").document(pid).collection("comments")
            .orderBy("time", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { comments ->
                comments.documents.forEach { comment ->
                    commentList.add(
                        Comment(
                            Comment.MAIN,
                            comment["text"].toString(),
                            comment["time"] as Long,
                            comment["writer"].toString()
                        )
                    )
                }
                onAdapter()
            }
    }

    private fun onAdapter() {
        if (commentList.size != 0) {
            adapter = CommentAdapter(commentList)
            post_comment_recycler.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            post_comment_recycler.adapter = adapter
        }
    }

    private fun submitComment() {
        fs.collection("posts").document(pid).collection("comments").add(
            hashMapOf(
                "writer" to uid,
                "text" to post_comment_edit_text.text.toString(),
                "like" to arrayListOf<String>(),
                "time" to System.currentTimeMillis()
            )
        ).addOnSuccessListener {
            getData()
        }
    }

    data class Comment(val type: Int, val text: String, val time: Long, val writer: String) {
        companion object {
            val MAIN = 0
        }
    }
}