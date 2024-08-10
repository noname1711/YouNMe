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
import com.example.younme.adapter.Comment
import com.example.younme.adapter.Status
import com.example.younme.adapter.StatusAdapter
import com.example.younme.adapter.User
import com.example.younme.login.LoginActivity
import com.google.android.gms.tasks.Task
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

        mAuth = FirebaseAuth.getInstance()

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        statusRecyclerView = findViewById(R.id.rvStatus)
        statusRecyclerView.layoutManager = LinearLayoutManager(this)
        statusList = mutableListOf()
        statusAdapter = StatusAdapter(this, statusList) // Chuyển context vào đây
        statusRecyclerView.adapter = statusAdapter

        mDbRefStatus = FirebaseDatabase.getInstance().getReference("status")
        mDbRefUser = FirebaseDatabase.getInstance().getReference("user")
        profileImageView = findViewById(R.id.profile_image)

        loadUserProfile()
        loadStatuses()

        val txtThink = findViewById<TextView>(R.id.txtThink)
        txtThink.setOnClickListener {
            val i = Intent(this@StatusActivity, InsertionStatusActivity::class.java)
            startActivity(i)
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.hope)

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
                    profileImageView.setImageResource(R.drawable.profile) //avatar mặc định
                }
            }.addOnFailureListener {
                Toast.makeText(this, R.string.failupload, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadStatuses() {
        mDbRefStatus.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                statusList.clear()

                val statusesToLoad = mutableListOf<Status>()
                val userFetches = mutableListOf<Task<DataSnapshot>>()
                val uidToUserMap = mutableMapOf<String, User>()

                // lấy status + data
                for (statusSnapshot in snapshot.children) {
                    for (statusIdSnapshot in statusSnapshot.children) {
                        val status = statusIdSnapshot.getValue(Status::class.java)
                        status?.let {
                            val uid = it.uid
                            if (uid != null) {
                                userFetches.add(mDbRefUser.child(uid).get())
                                statusesToLoad.add(it)
                            }
                        }
                    }
                }

                if (userFetches.isNotEmpty()) {
                    Tasks.whenAllComplete(userFetches).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Map user lấy UID
                            for (i in userFetches.indices) {
                                val userSnapshot = userFetches[i].result
                                val user = userSnapshot?.getValue(User::class.java)
                                user?.let {
                                    uidToUserMap[user.uid ?: ""] = user
                                }
                            }
                            // Update status
                            for (status in statusesToLoad) {
                                val user = uidToUserMap[status.uid ?: ""]
                                if (user != null) {
                                    status.profileImageUrl = user.profileImageUrl
                                    status.uid = user.name
                                }

                                loadCommentsForStatus(status)
                            }
                            statusList.addAll(statusesToLoad)
                        }
                        statusAdapter.notifyDataSetChanged()
                    }
                } else {
                    statusAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StatusActivity, R.string.fail_load_status, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCommentsForStatus(status: Status) {
        val commentsRef = mDbRefStatus.child(status.uid ?: "").child("comments")
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = mutableListOf<Comment>()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    if (comment != null) {
                        comments.add(comment)
                    }
                }
                status.comments = comments
                statusAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StatusActivity, R.string.fail_load_comments, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                mAuth.signOut()
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", false)
                editor.apply()

                val i = Intent(this@StatusActivity, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                finish()
                true
            }
            R.id.my_acc -> {
                startActivity(Intent(this@StatusActivity, ProfileActivity::class.java))
                true
            }
            R.id.setting -> {
                startActivity(Intent(this@StatusActivity, SettingsActivity::class.java))
                true
            }
            R.id.connect -> {
                startActivity(Intent(this@StatusActivity, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
