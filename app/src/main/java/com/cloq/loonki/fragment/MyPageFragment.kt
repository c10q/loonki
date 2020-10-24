package com.cloq.loonki.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloq.loonki.*
import com.cloq.loonki.adapter.HomeAdapter
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_square.*
import kotlinx.android.synthetic.main.fragment_my_page.*
import kotlinx.android.synthetic.main.fragment_my_page.view.*

val storage = FirebaseStorage.getInstance()

class MyPageFragment : Fragment() {
    private val dataList = arrayListOf<MainActivity.Post>()
    private lateinit var adapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView: View = inflater.inflate(R.layout.fragment_my_page, container, false)

        rootView.my_page_friends_btn.setOnClickListener {
            val intent = Intent(context, FriendActivity::class.java)
            startActivity(intent)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getProfile()
        getData()
    }

    private fun getData() {
        val uid = App.prefs.getPref("UID", "")
        fs.collection("posts").whereEqualTo("writer", uid).limit(5).get().addOnSuccessListener {posts ->
            if (posts.isEmpty) return@addOnSuccessListener
            posts.documents.forEach { post ->
                dataList.add(
                    MainActivity.Post(
                        MainActivity.Post.MAIN_CONTENT,
                        getTimeDiff(post["time"] as Long),
                        post["writer"].toString(),
                        post.id,
                        post["text"].toString(),
                        post["image"] as Boolean
                    )
                )
            }
            adapter = HomeAdapter(dataList)
            my_page_recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            my_page_recycler.adapter = adapter
            my_page_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(r: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(r, dx, dy)
                }
            })
        }
    }

    private fun getProfile() {
        val url = App.prefs.getPref("PROFILE_URL", "")
        if (url != "") {
            GlideApp.with(this@MyPageFragment)
                .load(url)
                .override(256, 256)
                .circleCrop()
                .into(my_page_profile_image)
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