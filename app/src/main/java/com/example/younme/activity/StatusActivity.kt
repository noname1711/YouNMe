package com.example.younme.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.example.younme.R
import com.example.younme.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class StatusActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Hope you enjoy my app"

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_status // Đặt mục Status làm mục được chọn
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_chat -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.navigation_status -> true
                else -> false
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item .itemId == R.id.logout){
            mAuth.signOut()

            val i = Intent(this@StatusActivity, LoginActivity::class.java)
            startActivity(i)
            finish()
            return true
        }
        if (item .itemId == R.id.my_acc){
            val i = Intent(this@StatusActivity,ProfileActivity::class.java)
            startActivity(i)
            return true
        }
        if (item .itemId == R.id.connect){
            val i = Intent(this@StatusActivity,AboutActivity::class.java)
            startActivity(i)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}