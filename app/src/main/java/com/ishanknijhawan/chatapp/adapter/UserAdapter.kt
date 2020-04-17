package com.ishanknijhawan.chatapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ishanknijhawan.chatapp.R
import com.ishanknijhawan.chatapp.helperClass.User
import com.ishanknijhawan.chatapp.ui.ChatActivity
import com.ishanknijhawan.chatapp.ui.PhotoView
import com.ishanknijhawan.chatapp.ui.imageURL
import kotlinx.android.synthetic.main.layout_users.view.*


val fUser = FirebaseAuth.getInstance().currentUser
val chatReference = Firebase.firestore.collection("Chat")
lateinit var lastMessage: String
lateinit var dialog: AlertDialog
lateinit var dialogView: View

class UserAdapter(val items: MutableList<User>, val context: Context, val chatBoolean: Boolean, val displayStatus: Boolean):
    RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(
                context
            ).inflate(
                R.layout.layout_users,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewUsers.text = items[position].username

        if (items[position].profilePicturePath == "default"){
            holder.imageView.setBackgroundResource(R.drawable.ic_user_profile)
        }
        else {
            Glide.with(context).load(items[position].profilePicturePath).into(holder.imageView)
        }

        if (displayStatus){
            holder.tvBio.text = items[position].bio
        }
        else {
            lastMessage = "No messages"
            chatReference.addSnapshotListener { querySnapshot, e ->
                for (document in querySnapshot!!.documents){
                    when {
                        document.getString("sender") == fUser!!.uid
                                && document.getString("receiver") == items[position].uid
                                && document.getString("image_url") == "default" -> {
                            lastMessage = document.getString("message").toString()
                        }
                        document.getString("sender") == items[position].uid
                                && document.getString("receiver") == fUser.uid
                                && document.getString("image_url") == "default" -> {
                            lastMessage = "You: ${document.getString("message").toString()}"
                        }
                        document.getString("sender") == fUser.uid
                                && document.getString("receiver") == items[position].uid
                                && document.getString("image_url") != "default" -> {
                            lastMessage = "Photo"
                        }
                        document.getString("sender") == items[position].uid
                                && document.getString("receiver") == fUser.uid
                                && document.getString("image_url") != "default" -> {
                            lastMessage = "You: Photo"
                        }
                    }
                }
                holder.tvBio.text = lastMessage
            }
        }

        if (chatBoolean){
            if (items[position].status == "online"){
                holder.imageOn.visibility = View.VISIBLE
                holder.imageOff.visibility = View.GONE
            }
            else {
                holder.imageOn.visibility = View.GONE
                holder.imageOff.visibility = View.GONE
            }
        }
        else {
            holder.imageOn.visibility = View.GONE
            holder.imageOff.visibility = View.GONE
        }

        holder.imageView.setOnClickListener {
            val intent = Intent(context, PhotoView::class.java)
            intent.putExtra("URI_USER", items[position].profilePicturePath)
            intent.putExtra("OPEN_CLA_USER", "openedfromua")
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
//            Firebase.firestore.collection("Chat").addSnapshotListener { querySnapshot, e ->
//                for (document in querySnapshot!!.documents){
//                    if ((document.getString("receiver") == FirebaseAuth.getInstance().currentUser?.uid)
//                        && (document.getString("sender") == items[position].uid)){
//
//                    }
//                }
//            }

            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("sender_name",items[position].username)
            intent.putExtra("sender_uid",items[position].uid)
            intent.putExtra("sender_dp_url",items[position].profilePicturePath)
            context.startActivity(intent)
        }
    }

}

class ViewHolder(view: View): RecyclerView.ViewHolder(view){
    val imageView: ImageView = view.profile_image
    val textViewUsers: TextView = view.tv_username
    val imageOn = view.iv_on
    val imageOff = view.iv_off
    val tvBio: TextView = view.tv_bio
}

