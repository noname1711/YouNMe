package com.example.younme.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.younme.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val click= findViewById<TextView>(R.id.click)
        click.setOnClickListener{
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/phoenix.khai.528"))
            startActivity(i)
        }
    }
}