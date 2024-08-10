package com.example.younme.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.younme.activity.ChatActivity
import com.example.younme.R
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(val context: Context, val userList: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.textName.text = currentUser.name

        if (currentUser.profileImageUrl != null) {
            Glide.with(context).load(currentUser.profileImageUrl).into(holder.profileImageView)
        }

        holder.itemView.setOnClickListener {
            val i = Intent(context, ChatActivity::class.java)
            i.putExtra("name", currentUser.name)
            i.putExtra("uid", currentUser.uid)
            i.putExtra("image",currentUser.profileImageUrl)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: CircleImageView = itemView.findViewById(R.id.img_profile)
        val textName: TextView = itemView.findViewById(R.id.txt_name)
    }
}
