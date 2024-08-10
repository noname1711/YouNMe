package com.example.younme.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.younme.R
import com.example.younme.adapter.Message
import com.example.younme.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    private lateinit var profileFriend: CircleImageView
    private lateinit var tvNameFriend: TextView

    var receiverRoom: String? = null
    var senderRoom: String?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val image = intent.getStringExtra("image")
        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        profileFriend = findViewById(R.id.profileFriend)
        tvNameFriend = findViewById(R.id.tvNameFriend)

        tvNameFriend.text = name
        Glide.with(this).load(image).into(profileFriend)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot){
                    messageList.clear()
                    for (postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError){

                }
            })

        sendButton.setOnClickListener{
            val message = messageBox.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this, R.string.empty, Toast.LENGTH_SHORT).show()
            } else {
                val messageObject = Message(message, senderUid)

                mDbRef.child("chats").child(senderRoom!!).child("messages").push().setValue(messageObject)
                    .addOnSuccessListener {
                        mDbRef.child("chats").child(receiverRoom!!).child("messages").push().setValue(messageObject)
                    }
                messageBox.setText("")
            }
        }
    }
}
