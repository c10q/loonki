package com.cloq.loonki

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cloq.loonki.Fragment.storage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profile_image_edit_link.setOnClickListener {
            val editIntent = Intent(this, EditImageActivity::class.java)
            startActivity(editIntent)
        }

        edit_profile_cancel.setOnClickListener {
            super.onBackPressed()
        }

        getProfile()
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

                        GlideApp.with(this@EditProfileActivity)
                            .load(fileName)
                            .override(256, 256)
                            .circleCrop()
                            .into(edit_profile_image)

                    } else {
                        GlideApp.with(this@EditProfileActivity)
                            .load(storage.reference.child("anonymous").child("anonymous.png"))
                            .override(256, 256)
                            .circleCrop()
                            .into(edit_profile_image)
                    }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}
