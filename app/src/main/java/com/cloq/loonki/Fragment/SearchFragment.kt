package com.cloq.loonki.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.cloq.loonki.Adapter.UserSearchAdapter
import com.cloq.loonki.MainActivity
import com.cloq.loonki.R
import com.cloq.loonki.UserInfo
import kotlinx.android.synthetic.main.fragment_search.*

var userList = arrayListOf<UserInfo>(
    UserInfo("이구찌", "fjei57fjsifsf", "23", "서울", "users_1"),
    UserInfo("김샤넬", "fjfegj23sifsf", "25", "수원", "users_2"),
    UserInfo("노루이비똥", "fjfegj23sifsf", "20", "창원", "users_3"),
    UserInfo("한디올", "fjfegj23sifsf", "21", "서울", "users_4")
)

class SearchFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mAdapter = UserSearchAdapter(this.context as MainActivity, userList) {
                user -> Toast.makeText(context, "클릭이벤트 ${user.userId}", Toast.LENGTH_SHORT).show()
        }
        mRecyclerView.adapter = mAdapter

        val gm = GridLayoutManager(context, 2)
        mRecyclerView.layoutManager = gm
        mRecyclerView.setHasFixedSize(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_search, container, false)
    }
}