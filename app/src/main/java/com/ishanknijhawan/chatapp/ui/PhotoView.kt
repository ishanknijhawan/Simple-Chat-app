package com.ishanknijhawan.chatapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.ishanknijhawan.chatapp.R
import kotlinx.android.synthetic.main.activity_photo_view.*

class PhotoView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)
        actionBar?.hide()

        if (intent.getStringExtra("OPEN_CLA") == "openedfromcla"){
            val url = intent.getStringExtra("URL")
            Glide.with(this).load(url).into(myZoomageView)
        }
        else if(intent.getStringExtra("OPEN_CLA_USER") == "openedfromua"){
            val url = intent.getStringExtra("URI_USER")
            if (url == "default")
                myZoomageView.setImageResource(R.drawable.ic_user_profile)
            else
                Glide.with(this).load(url).into(myZoomageView)
        }
    }
}
