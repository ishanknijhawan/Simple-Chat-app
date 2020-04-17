package com.ishanknijhawan.chatapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.Api
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.ishanknijhawan.chatapp.R
import com.ishanknijhawan.chatapp.adapter.ChatListAdapter
import com.ishanknijhawan.chatapp.helperClass.APIService
import com.ishanknijhawan.chatapp.helperClass.Message
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.chat_item_left.*
import java.util.*


class ChatActivity : AppCompatActivity() {

    val firestoreChat by lazy {
        Firebase.firestore.collection("Chat")
    }
    val chatMap = hashMapOf(
        "sender" to "",
        "receiver" to "",
        "message" to "",
        "timestamp" to "",
        "isSeen" to "false",
        "image_url" to "default"
    )
    val mChat = mutableListOf<Message>()
    val rightNow: Calendar = Calendar.getInstance()
    val fUser = FirebaseAuth.getInstance().currentUser
    val referenceUsers = Firebase.firestore.collection("users")
    lateinit var apiService: APIService
    lateinit var imageURLChat: Uri
    val userReferenceChat = Firebase.firestore.collection("Chat")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        actionBar?.hide()

        iv_progress.visibility = View.GONE
        tv_chat_Status.visibility = View.GONE

        ivHamburger2.setOnClickListener {
            finish()
        }


        val senderName: String? = intent.getStringExtra("sender_name")
        val senderUID: String? = intent.getStringExtra("sender_uid")
        val senderImageUrl: String? = intent.getStringExtra("sender_dp_url")
        val receiverUID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        var message: String
        val hour = rightNow.get(Calendar.HOUR)
        val minute = rightNow.get(Calendar.MINUTE)
        val timestamp = if (rightNow.get(Calendar.AM_PM) == Calendar.AM){
            "$hour:$minute AM"
        } else {
            "$hour:$minute PM"
        }

        //apiService = Client.getClient("https://fcm.gooogleapis.com/")!!.create(APIService::class.java)


        realtimeUpdateListener(senderUID, receiverUID, senderImageUrl)

        tv_chat_name.text = senderName.toString()

        if (senderImageUrl != "default")
            Glide.with(this).load(senderImageUrl).into(ic_chat_icon)

        referenceUsers.document(senderUID.toString()).get()
            .addOnSuccessListener {
                if (it.getString("status") == "online"){
                    tv_chat_Status.visibility = View.VISIBLE
                    tv_chat_Status.text = "online"
                }
                else {
                    tv_chat_Status.visibility = View.GONE
                }
            }

        rv_chats.layoutManager = LinearLayoutManager(this)

        iv_send.setOnClickListener {
            message = et_chat.text.toString()
            if (message.isEmpty())
                Toast.makeText(this, "Can't send empty message", Toast.LENGTH_SHORT).show()
            else
                sendMessage(senderUID, receiverUID, message, timestamp)
            et_chat.setText("")
        }

        iv_attach_image.setOnClickListener {
            openFileChooser()
        }

        seenMessage(senderUID)

    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,
            69
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 69 && resultCode == Activity.RESULT_OK
            && data != null && data.data != null){
            imageURLChat = data.data!!
            uploadFiletoFirebase()
        }
    }

    private fun uploadFiletoFirebase() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/chats/$filename")
        val hour = rightNow.get(Calendar.HOUR)
        val minute = rightNow.get(Calendar.MINUTE)
        val timestamp = if (rightNow.get(Calendar.AM_PM) == Calendar.AM){
            "$hour:$minute AM"
        } else {
            "$hour:$minute PM"
        }

        ref.putFile(imageURLChat)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
//                    iv_progress.visibility = View.VISIBLE
//                    iv_send.visibility = View.GONE

                    val handler = Handler()
                    handler.postDelayed({
                        iv_progress.progress = 0
                    },500)

                    chatMap["sender"] = intent.getStringExtra("sender_uid")!!
                    chatMap["receiver"] = FirebaseAuth.getInstance().currentUser?.uid.toString()
                    chatMap["message"] = ""
                    chatMap["timestamp"] = timestamp
                    chatMap["isSeen"] = "false"
                    chatMap["image_url"] = it.toString()
                    firestoreChat.document(System.currentTimeMillis().toString()).set(chatMap)

                    Toast.makeText(this, "Upload successfull", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnProgressListener {taskSnapshot ->
                iv_progress.visibility = View.VISIBLE
                iv_send.visibility = View.GONE
                Toast.makeText(this, "Uploading File...", Toast.LENGTH_SHORT).show()
                val progress = (100*taskSnapshot.bytesTransferred/taskSnapshot.totalByteCount)
                iv_progress.progress = progress.toInt()
            }
            .addOnCompleteListener {
                iv_progress.visibility = View.GONE
                iv_send.visibility = View.VISIBLE
            }
    }

    private fun realtimeUpdateListener(senderUID: String?, receiverUID: String, senderImageUrl: String?) {

        firestoreChat.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

            firestoreChat.get().addOnSuccessListener { documents ->
                mChat.clear()
                for (document in documents) {
                    if ((document.getString("sender") == senderUID) && (document.getString("receiver") == receiverUID)
                        || (document.getString("sender") == receiverUID) && (document.getString("receiver") == senderUID)
                    ) {
                        mChat.add(
                            Message(
                                document.getString("sender").toString(),
                                document.getString("receiver").toString(),
                                document.getString("message").toString(),
                                document.getString("timestamp").toString(),
                                document.getString("isSeen").toString(),
                                document.getString("image_url").toString()
                            )
                        )
                    }
                    rv_chats.layoutManager = LinearLayoutManager(this)
                    rv_chats.adapter =
                        ChatListAdapter(
                            mChat,
                            this,
                            senderImageUrl.toString()
                        )
                    rv_chats.scrollToPosition(rv_chats.adapter!!.itemCount-1)
                }
            }
        }
    }

    private fun seenMessage(userID: String?) {
        firestoreChat.addSnapshotListener { querySnapshot, e ->
            for (dataSnapshot in querySnapshot!!.documents){
                if ((dataSnapshot.getString("sender") == fUser!!.uid) and (dataSnapshot.getString("receiver") == userID)){
                    chatMap["isSeen"] = "true"
                    firestoreChat.document(dataSnapshot.id).update("isSeen","true")
                }
            }
        }
    }

    private fun sendMessage(senderUID: String?, receiverUID: String, message: String, timestamp: String) {
        chatMap["sender"] = senderUID!!
        chatMap["receiver"] = receiverUID
        chatMap["message"] = message
        chatMap["timestamp"] = timestamp
        chatMap["isSeen"] = "false"
        chatMap["image_url"] = "default"

        firestoreChat.document(System.currentTimeMillis().toString()).set(chatMap)

//        val msg = message
//        val reference = Firebase.firestore.collection("users").document(fUser!!.uid)
//        reference.addSnapshotListener { documentSnapshot, e ->
//            sendNotification(receiverUID, documentSnapshot!!.getString("username").toString(), msg)
//        }
    }

//    private fun sendNotification(receiverUID: String, username: String, msg: String) {
//        val reference = Firebase.firestore.collection("Tokens")
//        reference.addSnapshotListener { querySnapshot, e->
//            for (document in querySnapshot!!.documents){
//                val data = Data(fUser!!.uid, R.mipmap.ic_launcher, "$username: $msg","New message", receiverUID)
//                val sender = Sender(data, )
//
//                apiService.sendNotification()
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()
        referenceUsers.document(fUser!!.uid).update("status","online")
        firestoreChat.addSnapshotListener { querySnapshot, e ->
            for (dataSnapshot in querySnapshot!!.documents){
                if ((dataSnapshot.getString("sender") == fUser.uid) and (dataSnapshot.getString("receiver") == intent.getStringExtra("sender_uid"))){
                    chatMap["isSeen"] = "true"
                    firestoreChat.document(dataSnapshot.id).update("isSeen","true")
                }
            }
        }.remove()
    }

    override fun onPause() {
        super.onPause()
        referenceUsers.document(fUser!!.uid).update("status","offline")
        firestoreChat.addSnapshotListener { querySnapshot, e ->
            for (dataSnapshot in querySnapshot!!.documents){
                if ((dataSnapshot.getString("sender") == fUser.uid) and (dataSnapshot.getString("receiver") == intent.getStringExtra("sender_uid"))){
                    chatMap["isSeen"] = "true"
                    firestoreChat.document(dataSnapshot.id).update("isSeen","true")
                }
            }
        }.remove()
    }

}
