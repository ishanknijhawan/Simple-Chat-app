package com.ishanknijhawan.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_users.*

class UsersActivity : AppCompatActivity() {

    var mUsers = mutableListOf<User>()
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
                            document.getString("profile_picture_url").toString()
                        )
                    )
            }

            rv_users.layoutManager = LinearLayoutManager(this)
            rv_users.adapter = UserAdapter(mUsers, this)
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return super.onNavigateUp()
    }
}
