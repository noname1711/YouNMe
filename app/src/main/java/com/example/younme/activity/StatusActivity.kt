package com.example.younme.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.younme.R
import com.example.younme.adapter.Status
import com.example.younme.adapter.StatusAdapter
import com.example.younme.adapter.User
import com.example.younme.login.LoginActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class StatusActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var statusRecyclerView: RecyclerView
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var statusList: MutableList<Status>
    private lateinit var mDbRefStatus: DatabaseReference
    private lateinit var profileImageView: CircleImageView
    private lateinit var mDbRefUser: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Initialize RecyclerView
        statusRecyclerView = findViewById(R.id.rvStatus)
        statusRecyclerView.layoutManager = LinearLayoutManager(this)
        statusList = mutableListOf()
        statusAdapter = StatusAdapter(statusList)
        statusRecyclerView.adapter = statusAdapter

        // Initialize Database Reference
        mDbRefStatus = FirebaseDatabase.getInstance().getReference("status")
        mDbRefUser = FirebaseDatabase.getInstance().getReference("user")
        profileImageView = findViewById(R.id.profile_image)
        loadUserProfile()

        // Load statuses
        loadStatuses()

        val txtThink = findViewById<TextView>(R.id.txtThink)
        txtThink.setOnClickListener {
            val i = Intent(this@StatusActivity, InsertionStatusActivity::class.java)
            startActivity(i)
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Hope you enjoy my app"

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_status
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

    private fun loadUserProfile() {
        val currentUser = mAuth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            mDbRefUser.child(userId).get().addOnSuccessListener { snapshot ->
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                if (profileImageUrl != null) {
                    Glide.with(this).load(profileImageUrl).into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.profile) // Default avatar image
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadStatuses() {
        mDbRefStatus.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                statusList.clear()
                val userDbRef = mDbRefUser

                val statusesToLoad = mutableListOf<Status>()
                val userFetches = mutableListOf<DatabaseReference>()

                for (statusSnapshot in snapshot.children) {
                    val status = statusSnapshot.getValue(Status::class.java)
                    status?.let {
                        val uid = it.uid
                        if (uid != null) {
                            userFetches.add(userDbRef.child(uid))
                            statusesToLoad.add(it)
                        } else {
                            statusList.add(it)
                        }
                    }
                }

                if (userFetches.isNotEmpty()) {
                    val tasks = userFetches.map { it.get() }
                    Tasks.whenAllComplete(tasks).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (i in statusesToLoad.indices) {
                                val userSnapshot = tasks[i].result
                                val user = userSnapshot?.getValue(User::class.java)
                                if (user != null) {
                                    statusesToLoad[i].profileImageUrl = user.profileImageUrl
                                    statusesToLoad[i].text = user.name
                                }
                            }
                        }
                        statusList.addAll(statusesToLoad)
                        statusAdapter.notifyDataSetChanged()
                    }
                } else {
                    statusAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StatusActivity, "Failed to load statuses", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            mAuth.signOut()
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.apply()

            val i = Intent(this@StatusActivity, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
            return true
        }
        if (item.itemId == R.id.my_acc) {
            val i = Intent(this@StatusActivity, ProfileActivity::class.java)
            startActivity(i)
            return true
        }
        if (item .itemId == R.id.setting){
            val i = Intent(this@StatusActivity,SettingsActivity::class.java)
            startActivity(i)
            return true
        }
        if (item.itemId == R.id.connect) {
            val i = Intent(this@StatusActivity, AboutActivity::class.java)
            startActivity(i)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
