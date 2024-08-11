package com.example.younme.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.younme.R
import com.example.younme.activity.PhotoActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatusAdapter(private val context: Context, private val statusList: List<Status>) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    inner class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfileStatus: CircleImageView = itemView.findViewById(R.id.img_profileStatus)
        val txtNameStatus: TextView = itemView.findViewById(R.id.txtNameStatus)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val etComment: EditText = itemView.findViewById(R.id.et_comment)
        val btnSend: Button = itemView.findViewById(R.id.btn_send)
        val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.status_form, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val status = statusList[position]

        Glide.with(context)
            .load(status.profileImageUrl)
            .into(holder.imgProfileStatus)

        holder.txtNameStatus.text = status.uid
        holder.txtStatus.text = status.textStatus

        if (status.imageUrl != null) {
            Glide.with(context)
                .load(status.imageUrl)
                .into(holder.imgStatus)
            holder.imgStatus.setOnClickListener {
                // Hiển thị hình ảnh phóng to
                val intent = Intent(context, PhotoActivity::class.java).apply {
                    putExtra("imageUrl", status.imageUrl)
                }
                context.startActivity(intent)
            }
        } else {
            holder.imgStatus.visibility = View.GONE
        }

        // Times
        status.timestamp?.let {
            val dateFormat = SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault())
            holder.txtTime.text = dateFormat.format(Date(it))
        }

        // Load comments
        val commentsRef = FirebaseDatabase.getInstance().getReference("status").child(status.uid ?: "").child("comments")

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = mutableListOf<Comment>()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    if (comment != null) {
                        comments.add(comment)
                    }
                }
                holder.commentsRecyclerView.layoutManager = LinearLayoutManager(context)
                holder.commentsRecyclerView.adapter = CommentAdapter(context, comments)
                holder.commentsRecyclerView.visibility = if (comments.isEmpty()) View.GONE else View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        holder.btnSend.setOnClickListener {
            val commentText = holder.etComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val userRef = FirebaseDatabase.getInstance().getReference("user").child(currentUserId.toString())

                userRef.get().addOnSuccessListener { userSnapshot ->
                    val userProfileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java)
                    val userName = userSnapshot.child("name").getValue(String::class.java)

                    val newComment = Comment(
                        uid = currentUserId,
                        textComment = commentText,
                        timestamp = System.currentTimeMillis(),
                        userProfileImageUrl = userProfileImageUrl,
                        userName = userName
                    )
                    commentsRef.push().setValue(newComment).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            holder.etComment.text.clear()
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = statusList.size
}
