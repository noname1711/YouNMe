package com.example.younme.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.younme.R
import com.github.chrisbanes.photoview.PhotoView

class PhotoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        val photoView: PhotoView = findViewById(R.id.photoView)

        val imageUrl = intent.getStringExtra("imageUrl")
        Glide.with(this).load(imageUrl).into(photoView)
    }
}