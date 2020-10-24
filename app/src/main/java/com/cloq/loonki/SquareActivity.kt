package com.cloq.loonki

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_square.*
import com.cloq.loonki.MainActivity.Post
import com.cloq.loonki.adapter.HomeAdapter
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

class SquareActivity : AppCompatActivity() {
    lateinit var hotAdapter: HomeAdapter
    lateinit var recentAdapter: HomeAdapter
    private var loadingHot = false
    private var loadingRecent = false

    private val hotList = arrayListOf<Post>()
    private var hotCnt = 0
    private var hotEnd = false
    private lateinit var startAtHot: DocumentSnapshot

    private val recentList = arrayListOf<Post>()
    private var recentCnt = 0
    private var recentEnd = false
    private lateinit var startAtRecent: DocumentSnapshot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_square)
        setSupportActionBar(toolbar)
        toolbar_square.navigationIcon?.setTint(getColor(R.color.color4))

        squareBottom.onItemSelected = {
            when (it) {
                0 -> {
                    toolbar.title = "Hot"
                    square_hot_layout.visibility = View.VISIBLE
                    square_recent_layout.visibility = View.GONE
                }
                1 -> {
                    toolbar.title = "Recent"
                    square_hot_layout.visibility = View.GONE
                    square_recent_layout.visibility = View.VISIBLE
                }
            }
        }
        getHotFirst()
        getRecentFirst()
        square_hot_layout.setOnRefreshListener {
            hotList.removeAll(hotList)
            hotCnt = 0
            hotEnd = false
            getHotFirst(true)
        }
        square_recent_layout.setOnRefreshListener {
            recentList.removeAll(recentList)
            recentCnt = 0
            getRecentFirst()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    private fun getHotFirst(refresh: Boolean = false) {
        fs.collection("posts").orderBy("like", Query.Direction.DESCENDING)
            .limit(10).get().addOnSuccessListener { posts ->
                startAtHot = posts.documents[posts.size() - 1]
                hotCnt += posts.size()
                posts.forEach { post ->
                    hotList.add(
                        Post(
                            Post.MAIN_CONTENT,
                            getTimeDiff(post["time"] as Long),
                            post["writer"].toString(),
                            post.id,
                            post["text"].toString(),
                            post["image"] as Boolean
                        )
                    )
                }
                if (posts.size() == 10)
                    hotList.add(Post(Post.LOADING, "", "", "", ""))
                if (!refresh) onHotAdapter()
                else hotAdapter.notifyDataSetChanged()
            }
        square_hot_layout.isRefreshing = false
    }

    private fun getHot() {
        fs.collection("posts").orderBy("like", Query.Direction.DESCENDING).startAfter(startAtHot)
            .limit(10).get().addOnSuccessListener { posts ->
                if (posts.documents.size != 0) {
                    startAtHot = posts.documents[posts.size() - 1]
                    hotCnt += posts.size()
                }
                hotList.removeAt(hotList.size - 1)
                posts.forEach { post ->
                    hotList.add(
                        Post(
                            Post.MAIN_CONTENT,
                            getTimeDiff(post["time"] as Long),
                            post["writer"].toString(),
                            post.id,
                            post["text"].toString(),
                            post["image"] as Boolean
                        )
                    )
                }
                if (posts.size() == 10)
                    hotList.add(Post(Post.LOADING, "", "", "", ""))
                else hotEnd = true

                hotAdapter.notifyItemInserted(hotList.size - posts.size())
                loadingHot = false

            }
    }

    private fun getRecentFirst(refresh: Boolean = false) {
        fs.collection("posts").orderBy("time", Query.Direction.DESCENDING)
            .limit(10).get().addOnSuccessListener { posts ->
                startAtRecent = posts.documents[posts.size() - 1]
                recentCnt += posts.size()
                posts.forEach { post ->
                    recentList.add(
                        Post(
                            Post.MAIN_CONTENT,
                            getTimeDiff(post["time"] as Long),
                            post["writer"].toString(),
                            post.id,
                            post["text"].toString(),
                            post["image"] as Boolean
                        )
                    )
                }
                if (posts.size() == 10)
                    recentList.add(Post(Post.LOADING, "", "", "", ""))
                if (!refresh) onRecentAdapter()
                else recentAdapter.notifyDataSetChanged()
            }
        square_recent_layout.isRefreshing = false
    }

    private fun getRecent() {
        fs.collection("posts").orderBy("time", Query.Direction.DESCENDING).startAfter(startAtRecent)
            .limit(10).get().addOnSuccessListener { posts ->
                if (posts.documents.size != 0) {
                    startAtRecent = posts.documents[posts.size() - 1]
                    recentCnt += posts.size()
                }
                recentList.removeAt(recentList.size - 1)
                posts.forEach { post ->
                    recentList.add(
                        Post(
                            Post.MAIN_CONTENT,
                            getTimeDiff(post["time"] as Long),
                            post["writer"].toString(),
                            post.id,
                            post["text"].toString(),
                            post["image"] as Boolean
                        )
                    )
                }
                if (posts.size() == 10)
                    recentList.add(Post(Post.LOADING, "", "", "", ""))
                else recentEnd = true
                recentAdapter.notifyItemInserted(recentList.size - posts.size())
                loadingRecent = false
            }
    }

    private fun onHotAdapter() {
        if (hotList.size != 0) {
            hotAdapter = HomeAdapter(hotList)
            square_hot_recycler.adapter
            square_hot_recycler.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            square_hot_recycler.adapter = hotAdapter
            square_hot_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(r: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(r, dx, dy)
                    if (r.getChildAdapterPosition(r.getChildAt(r.childCount - 1)) > (hotCnt - 5) && !loadingHot && !hotEnd) {
                        loadingHot = true
                        getHot()
                    }
                }
            })
        }
    }

    private fun onRecentAdapter() {
        if (recentList.size != 0) {
            recentAdapter = HomeAdapter(recentList)
            square_recent_recycler.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            square_recent_recycler.adapter = recentAdapter
            square_recent_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(r: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(r, dx, dy)
                    if (r.getChildAdapterPosition(r.getChildAt(r.childCount - 1)) > (recentCnt - 5) && !loadingRecent && !recentEnd) {
                        loadingRecent = true
                        getRecent()
                    }
                }
            })
        }
    }

    private fun getTimeDiff(time: Long): String {
        val t = (System.currentTimeMillis() - time) / 1000

        return when {
            (t < 60) -> { // 1분
                t.toString() + "초 전"
            }
            (t < 60 * 60) -> { // 1시간
                (t / 60).toString() + "분 전"
            }
            (t < 60 * 60 * 24) -> { // 1일
                (t / (60 * 60)).toString() + "시간 전"
            }
            (t < 60 * 60 * 24 * 365) -> { // 1년
                (t / (60 * 60 * 24)).toString() + "일 전"
            }
            else -> {
                (t / (60 * 60 * 24 * 365)).toString() + "년 전"
            }
        }
    }
}