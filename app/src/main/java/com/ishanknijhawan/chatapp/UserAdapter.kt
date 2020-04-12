package com.ishanknijhawan.chatapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_users.view.*

class UserAdapter(val items: MutableList<User>, val context: Context):
    RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_users, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tectViewUsers.text = items[position].username

        holder.itemView.setOnClickListener {
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
    val tectViewUsers: TextView = view.tv_username
}
