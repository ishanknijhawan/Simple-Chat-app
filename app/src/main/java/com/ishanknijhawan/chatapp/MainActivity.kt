package com.ishanknijhawan.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_users.*

class MainActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    val userList = mutableListOf<String>()
    val mUserr = mutableListOf<User>()
    val mUser2 = mutableListOf<User>()
    val fUser = FirebaseAuth.getInstance().currentUser
    val reference = Firebase.firestore.collection("Chat")
    val reference2 = Firebase.firestore.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionBar = supportActionBar
        actionBar?.title = auth.currentUser?.email.toString()

        fab.setOnClickListener {
            val intent = Intent(this, UsersActivity::class.java)
            startActivity(intent)
        }

        reference.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//            reference.get().addOnSuccessListener {  document ->
//
//            }
            userList.clear()
            for (document in querySnapshot!!.documents){
                if (document.getString("sender") == fUser?.uid)
                    userList.add(document.getString("receiver").toString())
                else if (document.getString("receiver") == fUser?.uid)
                    userList.add(document.getString("sender").toString())
            }
        }

        reference2.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            mUserr.clear()

            for (document in querySnapshot!!.documents) {
                if (document.getString("uid").toString() != FirebaseAuth.getInstance().currentUser?.uid)
                    mUserr.add(
                        User(
                            document.getString("username").toString(),
                            document.getString("uid").toString(),
                            document.getString("email").toString(),
                            document.getString("profile_picture_url").toString()
                        )
                    )
            }

            var j = 0
            for (i in mUserr){
                if (i.uid == userList[j]){
                    mUser2.add(
                        User(
                            i.username,
                            i.uid,
                            i.email,
                            i.profilePicturePath
                        )
                    )
                }
                j++
            }
            rv_recent_chats.layoutManager = LinearLayoutManager(this)
            rv_recent_chats.adapter = UserAdapter(mUser2,this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuSignOut -> {
                val gsoo = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient =
                    GoogleSignIn.getClient(this, gsoo)

                val auth = FirebaseAuth.getInstance()
                auth.signOut()
                googleSignInClient.signOut()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
