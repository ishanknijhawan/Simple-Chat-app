package com.ishanknijhawan.chatapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ishanknijhawan.chatapp.R
import com.ishanknijhawan.chatapp.helperClass.User
import com.ishanknijhawan.chatapp.adapter.UserAdapter
import kotlinx.android.synthetic.main.activity_users.*

class UsersActivity : AppCompatActivity() {

    var mUsers = mutableListOf<User>()
    val fUser2 = FirebaseAuth.getInstance().currentUser
    val referenceX = Firebase.firestore.collection("users")

    val firestoreChat by lazy {
        Firebase.firestore.collection("users")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        val actionBar = supportActionBar
        actionBar!!.title = "Users"
        actionBar.setDisplayHomeAsUpEnabled(true)

        firestoreChat.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            mUsers.clear()

            for (document in querySnapshot!!.documents) {
                if (document.getString("uid").toString() != FirebaseAuth.getInstance().currentUser?.uid)
                    mUsers.add(
                        User(
                            document.getString("username").toString(),
                            document.getString("uid").toString(),
                            document.getString("email").toString(),
                            document.getString("profile_picture_url").toString(),
                            document.getString("status").toString(),
                            document.getString("bio").toString()
                        )
                    )
            }

            rv_users.layoutManager = LinearLayoutManager(this)
            rv_users.adapter =
                UserAdapter(
                    mUsers,
                    this,
                    false,
                    true
                )
        }

//        firestoreChat.get().addOnSuccessListener { documents ->
//            mUsers.clear()
//
//            for (document in documents) {
//                if (document.getString("uid").toString() != FirebaseAuth.getInstance().currentUser?.uid)
//                    mUsers.add(
//                        User(
//                            document.getString("username").toString(),
//                            document.getString("uid").toString(),
//                            document.getString("email").toString(),
//                            document.getString("profile_picture_url").toString()
//                        )
//                    )
//            }
//
//            rv_users.layoutManager = LinearLayoutManager(this)
//            rv_users.adapter = UserAdapter(mUsers, this)
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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
