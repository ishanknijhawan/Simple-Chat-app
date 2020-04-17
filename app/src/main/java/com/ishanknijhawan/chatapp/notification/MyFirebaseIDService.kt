package com.ishanknijhawan.chatapp.notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseIDService: FirebaseMessagingService() {
//    override fun onNewToken(p0: String) {
//        super.onNewToken(p0)
//        val firebaseUser = FirebaseAuth.getInstance().currentUser
//        val refreshToken = FirebaseInstanceId.getInstance().token
//
//        if (firebaseUser != null){
//            updateToken(refreshToken)
//        }
//    }
//
//    private fun updateToken(refreshToken: String?) {
//        val firebaseUser = FirebaseAuth.getInstance().currentUser
//        val databaseReference = Firebase.firestore.collection("Tokens")
//        val token = Token(refreshToken!!)
//
//    }
}