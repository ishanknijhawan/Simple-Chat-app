package com.ishanknijhawan.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
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
import com.ishanknijhawan.chatapp.helperClass.Message
import com.ishanknijhawan.chatapp.R
import com.ishanknijhawan.chatapp.ui.PhotoView
import kotlinx.android.synthetic.main.chat_item_left.view.*
import kotlinx.android.synthetic.main.chat_layout_right.view.*
import kotlinx.android.synthetic.main.chat_layout_right.view.tv_right
import java.lang.Exception
import java.net.InetAddress

const val LEFT_TYPE = 0
const val RIGHT_TYPE = 1

class ChatListAdapter(val items: MutableList<Message>, val context: Context, val imageUrl: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == RIGHT_TYPE)
            ViewHolderRight(
                LayoutInflater.from(
                    context
                ).inflate(
                    R.layout.chat_layout_right,
                    parent,
                    false
                )
            )
        else
            ViewHolderLeft(
                LayoutInflater.from(
                    context
                ).inflate(
                    R.layout.chat_item_left,
                    parent,
                    false
                )
            )
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        val fUser = FirebaseAuth.getInstance().currentUser
        return if (items[position].sender == fUser?.uid)
            LEFT_TYPE
        else
            RIGHT_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            RIGHT_TYPE -> {
                val headerViewHolder = holder as ViewHolderRight

                headerViewHolder.itemView.setOnClickListener {
                    if (items[position].image_url != "default"){
                        val intent = Intent(context, PhotoView::class.java)
                        intent.putExtra("OPEN_CLA", "openedfromcla")
                        intent.putExtra("URL", items[position].image_url)
                        context.startActivity(intent)
                    }
                }

                val cm =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
                val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

                if (items[position].image_url == "default"){
                    headerViewHolder.textMessage.visibility = View.VISIBLE
                    headerViewHolder.ivRight.visibility = View.GONE
                    headerViewHolder.textMessage.text = items[position].message
                    headerViewHolder.timeStamp.text = items[position].timestamp
                }
                else {
                    headerViewHolder.ivRight.visibility = View.VISIBLE
                    headerViewHolder.textMessage.visibility = View.GONE
                    Glide.with(context).load(items[position].image_url).into(headerViewHolder.ivRight)
                    headerViewHolder.timeStamp.text = items[position].timestamp
                }

                if (position == items.size - 1) {
                    if (isConnected) {
                        //Toast.makeText(context, "reaching here as well", Toast.LENGTH_SHORT).show()
                        if (items[position].isSeen == "true") {
                            headerViewHolder.txtSeen.text = "seen"
                        } else {
                            headerViewHolder.txtSeen.text = "Delivered"
                        }
                    } else {
                        headerViewHolder.txtSeen.text = "Sending..."
                    }
                } else {
                    headerViewHolder.txtSeen.visibility = View.GONE
                }
            }
            LEFT_TYPE -> {
                val headerViewHolderX = holder as ViewHolderLeft
                headerViewHolderX.textMessage.text = items[position].message
                headerViewHolderX.timeStamp2.text = items[position].timestamp

                headerViewHolderX.itemView.setOnClickListener {
                    if (items[position].image_url != "default"){
                        val intent = Intent(context, PhotoView::class.java)
                        intent.putExtra("URL", items[position].image_url)
                        intent.putExtra("OPEN_CLA", "openedfromcla")
                        context.startActivity(intent)
                    }
                }

                if (items[position].image_url == "default"){
                    headerViewHolderX.textMessage.visibility = View.VISIBLE
                    headerViewHolderX.ivLeft.visibility = View.GONE
                    headerViewHolderX.textMessage.text = items[position].message
                    headerViewHolderX.timeStamp2.text = items[position].timestamp
                }
                else {
                    headerViewHolderX.ivLeft.visibility = View.VISIBLE
                    headerViewHolderX.textMessage.visibility = View.GONE
                    Glide.with(context).load(items[position].image_url).into(headerViewHolderX.ivLeft)
                    headerViewHolderX.timeStamp2.text = items[position].timestamp
                }

                if (imageUrl == "default") {
                    headerViewHolderX.profileImage.setImageResource(R.drawable.ic_user_profile)
                } else {
                    Glide.with(context).load(imageUrl).into(holder.profileImage)
                }

            }
        }
    }
}

class ViewHolderRight(view: View) : RecyclerView.ViewHolder(view) {
    val textMessage: TextView = view.tv_right
    var timeStamp: TextView = view.tv_time
    val txtSeen: TextView = view.tv_seen
    val ivRight: ImageView = view.iv_message_right
}

class ViewHolderLeft(view: View) : RecyclerView.ViewHolder(view) {
    val textMessage: TextView = view.tv_right
    val profileImage = view.profile_image_2
    val timeStamp2: TextView = view.tv_time2
    val ivLeft: ImageView = view.iv_message_left
}


