package com.cloq.loonki


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_edit_image.*
import java.io.File

class EditImageActivity : AppCompatActivity() {

    private val OPEN_GALLERY = 1
    private var currentUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_image)

        openGallery()

        save_edit_profile_image.setOnClickListener{
            if(currentUri != null) saveProfileImage()
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
        when (requestCode){
            OPEN_GALLERY -> if(resultCode == Activity.RESULT_OK && data != null){
                val sourceUri: Uri? = data.data
                val file: File = getImageFile()
                val destinationUri = Uri.fromFile(file)
                launchImageCrop(sourceUri!!, destinationUri)
            } else {
                super.onBackPressed()
            }
            UCrop.REQUEST_CROP -> if(resultCode == Activity.RESULT_OK){

                    val result: Uri? = UCrop.getOutput(data!!)

                    edit_image_profile.setImageURI(result)
                    currentUri = result

                } else if(resultCode == UCrop.RESULT_ERROR){
                    Toast.makeText(this, "에러!", Toast.LENGTH_SHORT).show()
                } else {
                super.onBackPressed()
            }

        }

    }

    private fun launchImageCrop(sourceUri: Uri, destinationUri: Uri){
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1F, 1F)
                .withMaxResultSize(1024, 1024)
                .start(this)
    }

    @SuppressLint("SimpleDateFormat")
    fun saveProfileImage(){
        val uid = FirebaseAuth.getInstance().uid
        val storage = FirebaseStorage.getInstance()
        val fileName = "user_profile_${System.currentTimeMillis()}.png"
        val storageRef = storage.reference.child("users").child(uid.toString()).child("profile_main").child(fileName)

        auth?.uid?.let { FirebaseDatabase.getInstance().getReference("users").child(it).child("profile_img").setValue(fileName) }

        storageRef.putFile(currentUri!!).addOnSuccessListener {
            Toast.makeText(this, "업로드 완료!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
        }
    }
}