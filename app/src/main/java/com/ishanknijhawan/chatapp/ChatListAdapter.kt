package com.ishanknijhawan.chatapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.chat_item_left.view.*
import kotlinx.android.synthetic.main.chat_layout_right.view.*
import kotlinx.android.synthetic.main.chat_layout_right.view.tv_right

const val TYPE_RIGHT = 0
const val TYPE_LEFT = 1

class ChatListAdapter(val items: MutableList<Message>, val context: Context)
    : RecyclerView.Adapter<ViewHolder2>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder2 {
        return if (viewType == TYPE_LEFT)
            ViewHolder2(LayoutInflater.from(context).inflate(R.layout.chat_layout_right, parent, false))
        else
            ViewHolder2(LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder2, position: Int) {
        holder.textMessage.text = items[position].message
    }

    override fun getItemViewType(position: Int): Int {
        val fUser = FirebaseAuth.getInstance().currentUser
        return if (items[position].sender == fUser?.uid)
            TYPE_RIGHT
        else
            TYPE_LEFT
    }
}

class ViewHolder2(view: View): RecyclerView.ViewHolder(view) {
    val textMessage = view.tv_right
    val profileImage = view.profile_image_2
}

