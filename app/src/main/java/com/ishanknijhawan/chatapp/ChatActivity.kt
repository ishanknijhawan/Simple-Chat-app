package com.ishanknijhawan.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    val firestoreChat by lazy {
        Firebase.firestore.collection("Chat")
    }
    val chatMap = hashMapOf(
        "sender" to "",
        "receiver" to "",
        "message" to "",
        "timestamp" to "",
        "isSeen" to false
    )
    val mChat = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        val senderName: String? = intent.getStringExtra("sender_name")
        val senderUID: String? = intent.getStringExtra("sender_uid")
        val receiverUID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        var message = et_chat.text.toString()

        realtimeUpdateListener(senderUID, receiverUID, message)

        val actionbar = supportActionBar
        actionbar?.title = senderName

        rv_chats.layoutManager = LinearLayoutManager(this)

        iv_send.setOnClickListener {
            message = et_chat.text.toString()
            if (message.isEmpty())
                Toast.makeText(this, "Can't send empty message", Toast.LENGTH_SHORT).show()
            else
                sendMessage(senderUID, receiverUID, message)
            et_chat.setText("")
        }
    }

    private fun realtimeUpdateListener(senderUID: String?, receiverUID: String, message: String) {

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
                                "",
                                false
                            )
                        )
                    }
                    rv_chats.layoutManager = LinearLayoutManager(this)
                    rv_chats.adapter = ChatListAdapter(mChat, this)
                    rv_chats.scrollToPosition(rv_chats.adapter!!.itemCount-1)
                }
            }
        }
    }

    private fun sendMessage(senderUID: String?, receiverUID: String, message: String) {
        chatMap["sender"] = senderUID!!
        chatMap["receiver"] = receiverUID
        chatMap["message"] = message
        firestoreChat.document(System.currentTimeMillis().toString()).set(chatMap)
    }

}
