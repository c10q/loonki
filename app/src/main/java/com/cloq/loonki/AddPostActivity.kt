package com.cloq.loonki

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.cloq.loonki.fragment.storage
import com.example.awesomedialog.*
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_edit_image.*
import java.io.File

class AddPostActivity : AppCompatActivity() {
    private val OPEN_GALLERY = 1
    private var currentUri: Uri? = null

    lateinit var uid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        val userId = App.prefs.getPref("UID", "")
        when(App.prefs.getPref("UID", "")) {
            "" -> return
            else -> uid = userId
        }

        add_post_add_image_btn.setOnClickListener {
            openGallery()
        }

        add_post_clear_btn.setOnClickListener {
            super.onBackPressed()
        }

        add_post_save_btn.setOnClickListener {
            checkSave()
        }

        add_post_image_preview.setOnClickListener {
            openGallery()
        }

    }

    private fun openGallery() {
        val pictureIntent = Intent(Intent.ACTION_GET_CONTENT)
        pictureIntent.type = "image/*" // 1

        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE) // 2

        val mimeTypes = arrayOf("image/jpeg", "image/png") // 3

        pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        startActivityForResult(
            Intent.createChooser(pictureIntent, "Select Picture"),
            OPEN_GALLERY
        )
    }

    private fun getImageFile(): File {
        val imageFileName = "JPEG_" + System.currentTimeMillis() + "_"
        val dir = getExternalFilesDir("my_images")
        return File.createTempFile(
            imageFileName, ".jpg", dir
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OPEN_GALLERY -> if (resultCode == Activity.RESULT_OK && data != null) {
                val sourceUri: Uri? = data.data
                val file: File = getImageFile()
                val destinationUri = Uri.fromFile(file)
                launchImageCrop(sourceUri!!, destinationUri)
            } else {
                super.onBackPressed()
            }
            UCrop.REQUEST_CROP -> if (resultCode == Activity.RESULT_OK) {

                val result: Uri? = UCrop.getOutput(data!!)

                add_post_image_preview.setImageURI(result)
                add_post_image_preview.visibility = View.VISIBLE
                add_post_image_preview.layoutParams.height = add_post_image_preview.width
                add_post_image_layout.visibility = View.GONE
                currentUri = result

            } else if (resultCode == UCrop.RESULT_ERROR) {
                Toast.makeText(this, "에러!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchImageCrop(sourceUri: Uri, destinationUri: Uri) {
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1F, 1F)
            .withMaxResultSize(1024, 1024)
            .start(this)
    }

    private fun checkSave() {
        AwesomeDialog.build(this)
            .title("저장할까요?", titleColor = Color.parseColor("#EDD7C3"))
            .background(R.drawable.dialog_bg)
            .onPositive(
                "저장 하기!",
                buttonBackgroundColor = R.drawable.dialog_btn_bg,
                textColor = Color.parseColor("#1A253F")
            ) { savePost() }
            .onNegative(
                "취소",
                buttonBackgroundColor = R.drawable.dialog_btn_bg2,
                textColor = Color.parseColor("#1A253F")
            ) { Log.d("TAG", "negative ") }
    }

    private fun savePost() {
        val text = add_post_edit_text.text.toString()
        add_post_edit_text.isEnabled = false

        val fileName = "image.png"

        fs.collection("posts").add(
            hashMapOf(
                "writer" to uid,
                "text" to text,
                "like" to 0,
                "time" to System.currentTimeMillis(),
                "image" to (currentUri != null)
            ) as Map<String, Any>
        ).addOnSuccessListener {
            fs.collection("users").document(uid).collection("posts").document(it.id).set(
                hashMapOf(
                    "time" to System.currentTimeMillis()
                ) as Map<String, Any>
            )
            if (currentUri != null) {
                val storageRef = storage.reference.child("posts/${it.id}/$fileName")
                storageRef.putFile(currentUri!!)
            }
        }
    }
}