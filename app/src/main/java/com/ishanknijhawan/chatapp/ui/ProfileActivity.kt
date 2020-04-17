package com.ishanknijhawan.chatapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.ishanknijhawan.chatapp.R
import kotlinx.android.synthetic.main.activity_profile.*
import java.util.*

const val IMAGE_REQUEST = 1
lateinit var imageURL: Uri
var upload = ""
val fUser2 = FirebaseAuth.getInstance().currentUser
val referenceX = Firebase.firestore.collection("users")


class ProfileActivity : AppCompatActivity() {

    val fUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val userReference = Firebase.firestore.collection("users").document(fUser)
    val storageReference = FirebaseStorage.getInstance().getReference("uploads")
    var downloadUri = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvUpload.visibility = View.GONE
        progressBar.visibility = View.GONE

        userReference.addSnapshotListener { documentSnapshot, e ->
            etProfileName.setText(documentSnapshot!!["username"].toString())
            etBio.setText(documentSnapshot["bio"].toString())
            tvEmail.text = documentSnapshot["email"].toString()
            if (documentSnapshot["profile_picture_url"] == "default"){
                imageViewProfile.setBackgroundResource(R.drawable.ic_user_profile)
            }
            else {
                Glide.with(applicationContext).load(documentSnapshot["profile_picture_url"]).into(imageViewProfile)
            }
        }

        btnSave.setOnClickListener {
            userReference.update("username",etProfileName.text.toString())
            userReference.update("bio",etBio.text.toString())
            Toast.makeText(this, "values updated", Toast.LENGTH_SHORT).show()
            //uploadFile()
            //uploadFiletoFirebase()
        }

        iv_edit.setOnClickListener {
            openFileChooser()
        }
    }

    private fun uploadFiletoFirebase() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(imageURL)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    val handler = Handler()
                    handler.postDelayed({
                        progressBar.progress = 0
                        tvUpload.text = "Uploading 0%"
                    },500)
                    userReference.update("profile_picture_url",it.toString())
                    Toast.makeText(this, "Upload successfull", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnProgressListener {taskSnapshot ->
                tvUpload.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                val progress = (100*taskSnapshot.bytesTransferred/taskSnapshot.totalByteCount)
                progressBar.progress = progress.toInt()
                tvUpload.text = "Uploading $progress%"
            }
            .addOnCompleteListener {
                tvUpload.visibility = View.GONE
                progressBar.visibility = View.GONE
            }
    }

    private fun getFileExtension(uri: Uri): String {
        val cr = contentResolver
        val mimeType = MimeTypeMap.getSingleton()
        return mimeType.getExtensionFromMimeType(cr.getType(uri)).toString()
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,
            IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null){
            imageURL = data.data!!
            Glide.with(applicationContext).load(imageURL).into(imageViewProfile)
            uploadFiletoFirebase()
        }
    }

    override fun onResume() {
        super.onResume()
        referenceX.document(fUser2!!.uid).update("status","online")
    }

    override fun onPause() {
        super.onPause()
        referenceX.document(fUser2!!.uid).update("status","offline")
    }
}
