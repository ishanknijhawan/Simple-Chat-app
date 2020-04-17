package com.ishanknijhawan.chatapp.ui

import android.content.Intent
import android.media.session.MediaSession
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.ishanknijhawan.chatapp.R
import com.ishanknijhawan.chatapp.adapter.UserAdapter
import com.ishanknijhawan.chatapp.helperClass.User
import com.ishanknijhawan.chatapp.notification.Token
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    val userList = mutableListOf("")
    val mUserr = mutableListOf<User>()
    val fUser = FirebaseAuth.getInstance().currentUser
    val reference = Firebase.firestore.collection("Chat")
    val reference2 = Firebase.firestore.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageViewHide.visibility = View.GONE
        textViewHide.visibility = View.GONE

        val actionBar = supportActionBar
        actionBar?.title = "ChatApp"

        fab.setOnClickListener {
            val intent = Intent(this, UsersActivity::class.java)
            startActivity(intent)
        }

        rv_recent_chats.layoutManager = LinearLayoutManager(this)
        reference.addSnapshotListener { querySnapshot, e ->
            userList.clear()
            for (document in querySnapshot!!.documents) {
                if (document.getString("sender") == fUser!!.uid)
                    userList.add(document.getString("receiver").toString())
                else if (document.getString("receiver") == fUser.uid)
                    userList.add(document.getString("sender").toString())
            }

            if (userList.isEmpty()){
                imageViewHide.visibility = View.VISIBLE
                textViewHide.visibility = View.VISIBLE
            }

            readChats2()
        }
        //updateToken(FirebaseInstanceId.getInstance().token.toString())
    }

    private fun readChats2() {
        val nonRepeated = userList.toMutableSet()

        reference2.addSnapshotListener { querySnapshot, e ->
            mUserr.clear()
            for (document in querySnapshot!!.documents){
                if (nonRepeated.isNotEmpty()){
                    if (nonRepeated.contains(document.getString("uid"))){
                        mUserr.add(
                            User(
                                document.getString("username").toString(),
                                document.getString("uid").toString(),
                                document.getString("email").toString(),
                                document.getString("profile_picture_url").toString(),
                                document.getString("status").toString(),
                                document.getString("bio").toString(),
                                document.getString("typingStatus").toString()
                            )
                        )
                    }
                    imageViewHide.visibility = View.GONE
                    textViewHide.visibility = View.GONE
                }
                else {
//                    mUserr.add(
//                        User(
//                            document.getString("username").toString(),
//                            document.getString("uid").toString(),
//                            document.getString("email").toString(),
//                            document.getString("profile_picture_url").toString(),
//                            document.getString("status").toString(),
//                            document.getString("bio").toString()
//                        )
//                    )
//                    break
                }
            }
            rv_recent_chats.adapter = UserAdapter(mUserr, this, true, false)
        }
    }

//    private fun updateToken(token: String){
//        val reference = Firebase.firestore.collection("Tokens")
//        val token1 = Token(token)
//        val tokenMap = hashMapOf("token" to token1)
//        reference.document(fUser?.uid.toString()).set(tokenMap)
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSignOut -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Sign Out")
                builder.setMessage("Do you want to sign out from ChatApp ?")
                builder.setIcon(android.R.drawable.ic_dialog_alert)


                builder.setPositiveButton("Sign out"){dialogInterface, i ->
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
                        .or(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }

                builder.setNegativeButton("Cancel"){dialogInterface, i ->  

                }

                val alertDialog: AlertDialog = builder.create()

                alertDialog.setCancelable(true)
                alertDialog.show()

            }
            R.id.menuProfile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        reference2.document(fUser!!.uid).update("status", "online")
    }

    override fun onPause() {
        super.onPause()
        reference2.document(fUser!!.uid).update("status", "offline")
    }

}
