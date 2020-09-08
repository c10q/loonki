package com.cloq.loonki.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cloq.loonki.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_my_page.*
import kotlinx.android.synthetic.main.fragment_my_page.view.*

val storage = FirebaseStorage.getInstance()

class MyPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth?.currentUser?.reload()

        val rootView: View = inflater.inflate(R.layout.fragment_my_page, container, false)

        rootView.my_page_profile_edit_btn.setOnClickListener() {
            val intent = Intent(context, EditProfileActivity::class.java)
            startActivity(intent)
        }
        getProfile()

        return rootView
    }

    private fun getProfile() {
        val uid = auth?.uid.toString()
        val fileRef = db.reference.child("users").child(uid)
        fileRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("profile_img").exists()) {
                    val fileName = storage.reference
                        .child("users")
                        .child(uid)
                        .child("profile_main")
                        .child(dataSnapshot.child("profile_img").value.toString())

                    GlideApp.with(this@MyPageFragment)
                        .load(fileName)
                        .override(256, 256)
                        .circleCrop()
                        .into(my_page_profile_image)

                } else {
                    GlideApp.with(this@MyPageFragment)
                        .load(storage.reference.child("anonymous").child("anonymous.png"))
                        .override(256, 256)
                        .circleCrop()
                        .into(my_page_profile_image)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

}