package com.cloq.loonki

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cloq.loonki.dialog.RegionDialog
import kotlinx.android.synthetic.main.activity_social_register.*

class SocialRegisterActivity : AppCompatActivity() {
    private lateinit var user: String
    private lateinit var email: String
    private lateinit var type: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_register)

        user = intent.getStringExtra("UID").toString()
        email = intent.getStringExtra("EMAIL").toString()
        type = intent.getStringExtra("TYPE").toString()

        social_reg_gender_f.isChecked = true

        val selectedRegion = TagInfo(
            "전체",
            "전체"
        )

        social_reg_region.text = selectedRegion.region

        val regionDialog = RegionDialog(this, 1)
        social_reg_region_select_btn.setOnClickListener {
            regionDialog.start()
            regionDialog.finish {
                if (it.region == "전체") {
                    social_reg_region.text = it.region
                } else {
                    social_reg_region.text = it.region
                    social_reg_area.text = it.area
                }
            }
        }

        social_reg_start_btn.setOnClickListener {
            save()
        }
    }

    private fun gender() : String {
        return when {
            social_reg_gender_f.isChecked -> "여자"
            social_reg_gender_m.isChecked -> "남자"
            else -> ""
        }
    }

    private fun save() {
        fs.collection("users").document(user).set(
            hashMapOf<String, Any>(
                "acc_type" to type,
                "email" to email,
                "name" to social_reg_nickname.text.toString(),
                "region" to social_reg_region.text.toString(),
                "area" to social_reg_area.text.toString(),
                "gender" to gender(),
                "age" to social_reg_age.text.toString().toInt(),
                "valid" to true
            )
        ).addOnCompleteListener {
            App.prefs.setPref("UID", user)
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
