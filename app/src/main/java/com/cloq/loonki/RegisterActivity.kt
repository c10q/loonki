package com.cloq.loonki

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*
import java.net.PasswordAuthentication

val FIRESTORE_TAG = "파이어스토어"

data class User(
    var username: String? = "",
    var email: String? = "",
    var age: String? = "",
    var region: String? = "",
    var gender: String? = "",
    var accountType: String? = ""
)

class Register : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSpinner()

        auth = FirebaseAuth.getInstance()

        confirm_register.setOnClickListener() {
            createEmail()
        }


    }

    private fun gender(): String {
        return if (register_gender_radio_f.isChecked) "여자"
        else if (register_gender_radio_m.isChecked) "남자"
        else "모르겠어"
    }

    private fun setSpinner() {
        val regionList = resources.getStringArray(R.array.spinner_region)

        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            regionList
        )

        spinner_region.adapter = arrayAdapter
        spinner_region.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                Toast.makeText(applicationContext, "선택완료!.", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    private fun createEmail() {
        auth.createUserWithEmailAndPassword(
            register_email.text.toString(),
            register_password.text.toString()
        )
            ?.addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    """auth!!.currentUser?.sendEmailVerification()?.addOnCompleteListener { verify ->
                        if (verify.isSuccessful) {
                            saveData(FirebaseAuth.getInstance().uid.toString())
                            Toast.makeText(
                                baseContext,
                                "인증이 완료됐습니다. ${FirebaseAuth.getInstance().uid}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(baseContext, "이메일 인증을 진행해주세요.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }"""
                    saveData(FirebaseAuth.getInstance().uid.toString())
                    Toast.makeText(baseContext, "추가 완료!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, "인증에 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveData(uid: String) {

        val user = hashMapOf(
            "name" to register_nickname.text.toString(),
            "email" to register_email.text.toString(),
            "age" to register_user_age.text.toString(),
            "region" to spinner_region.selectedItem.toString(),
            "gender" to gender(),
            "acc_type" to "Email"
        )

        fs.collection("users").document(uid).set(user as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                Log.d(FIRESTORE_TAG, "스냅샷이 추가됐습니다: $documentReference")
            }
            .addOnFailureListener { e ->
                Log.w(FIRESTORE_TAG, "데이터 추가에 실패했습니다.", e)
            }
    }
}