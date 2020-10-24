package com.cloq.loonki

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cloq.loonki.dialog.RegionDialog
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.fragment_my_page.*

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

        edit_region_link.setOnClickListener {
            RegionDialog(this).start()
        }

        getUserInfo()
    }

    override fun onStart() {
        super.onStart()
        getProfile()
    }

    private fun getProfile() {
        val url = App.prefs.getPref("PROFILE_URL", "")
        GlideApp.with(this)
            .load(url)
            .override(256, 256)
            .circleCrop()
            .into(edit_profile_image)
    }

    private fun getUserInfo() {
        val uid = App.prefs.getPref("UID", "no user")
        fs.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                edit_profile_name.setText(snapshot.get("name").toString())
                edit_profile_age.setText(snapshot.get("age").toString())
                edit_profile_region.text = snapshot.get("region").toString()
                edit_profile_area.text = snapshot.get("area").toString()
                edit_profile_gender_text.text = snapshot.get("gender").toString()
            }
        }
    }
}
