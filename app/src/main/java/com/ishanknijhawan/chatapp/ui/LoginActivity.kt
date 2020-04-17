package com.ishanknijhawan.chatapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ishanknijhawan.chatapp.R
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    val RC_SIGN_IN = 1
    val RC_SIGN_IN_2 = 2
    lateinit var auth: FirebaseAuth
    lateinit var gso: GoogleSignInOptions
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var db: FirebaseFirestore
    lateinit var data: MutableMap<String, Any>
    var test = ""

    val firestoreUserList by lazy {
        Firebase.firestore.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        db = Firebase.firestore

        btnRegister.setOnClickListener {
            signIn()
        }

        btnSignIn.setOnClickListener {
            signIn2()
        }

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signIn2() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN_2)
    }

    private fun updateUI2(currentUser: FirebaseUser?){

        val docref = Firebase.firestore.collection("users").document(currentUser!!.uid)

        docref.get().addOnCompleteListener {
            if (it.isSuccessful){
                val documentSnapshot = it.result
                if (documentSnapshot!!.exists()){
                    val userList = hashMapOf(
                        "username" to currentUser.displayName.toString(),
                        "uid" to currentUser.uid,
                        "profile_picture_url" to "default",
                        "email" to currentUser.email.toString(),
                        "status" to "online",
                        "bio" to "Heyy I am using ChatApp!",
                        "typingStatus" to "noOne"
                    )

                        userList["username"] = documentSnapshot.getString("username").toString()
                        userList["uid"] = currentUser.uid
                        userList["profile_picture_url"] = documentSnapshot.getString("profile_picture_url").toString()
                        userList["email"] = currentUser.email.toString()
                        userList["status"] = "online"
                        userList["bio"] = documentSnapshot.getString("bio").toString()
                        userList["typingStatus"] = documentSnapshot.getString("typingStatus").toString()

                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK).or(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        Toast.makeText(this, "Welcome back ${userList["username"]}", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "User not registered, please register first", Toast.LENGTH_LONG).show()
                    val gsoo = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient =
                        GoogleSignIn.getClient(this, gsoo)

                    val auth = FirebaseAuth.getInstance()
                    auth.signOut()
                    googleSignInClient.signOut()
                }
            }
            else {
                Toast.makeText(this, "Verification failed, please try again", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        val docref = Firebase.firestore.collection("users").document(currentUser!!.uid)

        docref.get().addOnCompleteListener {
            if (it.isSuccessful){
                val document = it.result
                if (document!!.exists()){
                    Toast.makeText(this, "User already registered, please log in", Toast.LENGTH_LONG).show()
                    val gsoo = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient =
                        GoogleSignIn.getClient(this, gsoo)

                    val auth = FirebaseAuth.getInstance()
                    auth.signOut()
                    googleSignInClient.signOut()
                }
                else {
                    val userList = hashMapOf(
                        "username" to currentUser.displayName.toString(),
                        "uid" to currentUser.uid,
                        "profile_picture_url" to "default",
                        "email" to currentUser.email.toString(),
                        "status" to "online",
                        "bio" to "Heyy I am using ChatApp!",
                        "typingStatus" to "noOne"
                    )

//                    Firebase.firestore.collection("users").addSnapshotListener { querySnapshot, e->
//                        for (document in querySnapshot!!.documents){
//                            if (document.getString("uid") == currentUser!!.uid){
//                                test = "no"
//                                break
//                            }
//                        }
//                    }
//
//                    if (test == "no"){
//                        Toast.makeText(this, "User already registered", Toast.LENGTH_SHORT).show()
//                    }
//                    else {
                        firestoreUserList.get()
                            .addOnSuccessListener {documentSnapshot ->
                                if (documentSnapshot != null){
                                    //document already exists, do nothing
                                    firestoreUserList.set(userList)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Data added !", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                else {
                                    firestoreUserList.set(userList)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Data added !", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }

                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK).or(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        Toast.makeText(this, "Welcome ${currentUser?.displayName}", Toast.LENGTH_SHORT).show()
                    }
            }
            else {
                Toast.makeText(this, "Verification failed, please try again", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e)
            }
        }
        else if(requestCode == RC_SIGN_IN_2){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle2(account!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT)
                        .show()
                    Toast.makeText(this, "null", Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }
    }

    private fun firebaseAuthWithGoogle2(acct: GoogleSignInAccount) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI2(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT)
                        .show()
                    Toast.makeText(this, "null", Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }
    }
}
