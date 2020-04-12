package com.ishanknijhawan.chatapp

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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    val RC_SIGN_IN = 1
    lateinit var auth: FirebaseAuth
    lateinit var gso: GoogleSignInOptions
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var db: FirebaseFirestore
    lateinit var data: MutableMap<String, Any>

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

        btnSignIn.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        val userList = hashMapOf(
            "username" to currentUser?.displayName.toString(),
            "uid" to currentUser?.uid.toString(),
            "profile_picture_url" to "default",
            "email" to currentUser?.email.toString()
        )

        firestoreUserList.set(userList)
            .addOnSuccessListener {
                Toast.makeText(this, "Data added !", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        Toast.makeText(this, "Welcome ${currentUser?.displayName}", Toast.LENGTH_SHORT).show()

//        var list2 = ""
//        var list3 = mutableListOf<String>()

//        firestoreUserList.get()
//            .addOnSuccessListener { document->
//                if (document != null){
//                    //document.data?.set("list", FirebaseAuth.getInstance().currentUser?.email.toString())
//                    list2 = document.data!!["list"].toString()
//
//                    Toast.makeText(this,"value of document.data is ${document.data!!["list"]}",Toast.LENGTH_LONG).show()
//                    Toast.makeText(this,"value of list2 is $list2",Toast.LENGTH_LONG).show()
//
//                    if (list2.contains(",")){
//                        list3 = list2.split(",").toMutableList()
//                    }
//                    else if (list2.length > 5){
//                        list3 = mutableListOf(list2)
//                    }
//                    else {
//                        list3 = mutableListOf()
//                    }
//                    Toast.makeText(this,"value of list3 is $list3",Toast.LENGTH_LONG).show()
//
//                    val finalList = arrayListOf<String>()
//
//                    if (list3.isNotEmpty()){
//                        for (i in list3.indices){
//                            when {
//                                list3[i].contains("[") and list3[i].contains("]") -> {
//                                    val one = list3[i].replace("[","").trim()
//                                    val two = one.replace("]","").trim()
//                                    finalList.add(two.trim())
//                                }
//                                list3[i].contains("[") -> finalList.add(list3[i].replace("[","").trim())
//                                list3[i].contains("]") -> finalList.add(list3[i].replace("]","").trim())
//                                else -> finalList.add(list3[i].trim())
//                            }
//                        }
//                        Toast.makeText(this, "value of finallist is $finalList",Toast.LENGTH_LONG).show()
//
//                        for (i in finalList.toSet().indices){
//                            userList["list"]?.add(finalList[i])
//                        }
//
//                        Toast.makeText(this,"value of userlist is $userList",Toast.LENGTH_LONG).show()
//                    }
//
//                    //userList["list"]?.add(FirebaseAuth.getInstance().currentUser?.email.toString())
//                    userList["list"]?.add(FirebaseAuth.getInstance().currentUser?.displayName.toString())
//
//                    firestoreUserList.set(userList)
//                        .addOnSuccessListener {
//                            //Toast.makeText(this, "Data added", Toast.LENGTH_SHORT).show()
//                        }
//                        .addOnFailureListener {
//                            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
//                        }
//
//                    val intent = Intent(this, MainActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
//                    Toast.makeText(this, "Welcome ${currentUser?.displayName}", Toast.LENGTH_SHORT).show()
//                }
//            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
}
