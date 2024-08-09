package com.example.younme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.younme.R
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*


class StatusAdapter(private val statusList: List<Status>) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    inner class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfileStatus: CircleImageView = itemView.findViewById(R.id.img_profileStatus)
        val txtNameStatus: TextView = itemView.findViewById(R.id.txtNameStatus)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val etComment: EditText = itemView.findViewById(R.id.et_comment)
        val btnSend: Button = itemView.findViewById(R.id.btn_send)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.status_form, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val status = statusList[position]

        // Load profile image using Glide
        Glide.with(holder.itemView.context)
            .load(status.profileImageUrl)
            .into(holder.imgProfileStatus)

        // Set UID as user name (if you need to fetch actual user name, you'll need to fetch from Firebase Auth or another source)
        holder.txtNameStatus.text = status.uid

        // Set other status details
        holder.txtStatus.text = status.textStatus

        // Load status image if available
        if (status.imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(status.imageUrl)
                .into(holder.imgStatus)
        } else {
            holder.imgStatus.visibility = View.GONE // Hide image view if no image
        }

        // Format and set timestamp
        status.timestamp?.let {
            val dateFormat = SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault())
            holder.txtTime.text = dateFormat.format(Date(it))
        }

        // Handle comments and send button actions
        holder.btnSend.setOnClickListener {
            // Handle send comment action
        }
    }

    override fun getItemCount(): Int = statusList.size
}
