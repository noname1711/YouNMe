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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.status_form, parent, false)
        return StatusViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val status = statusList[position]
        holder.bind(status)
    }

    override fun getItemCount(): Int {
        return statusList.size
    }

    class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfileStatus: CircleImageView = itemView.findViewById(R.id.img_profileStatus)
        private val txtNameStatus: TextView = itemView.findViewById(R.id.txtNameStatus)
        private val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        private val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
        private val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        private val etComment: EditText = itemView.findViewById(R.id.et_comment)
        private val btnSend: Button = itemView.findViewById(R.id.btn_send)

        fun bind(status: Status) {
            // Display status content
            txtStatus.text = status.text

            // Format timestamp
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val formattedTime = sdf.format(Date(status.timestamp ?: 0))
            txtTime.text = formattedTime

            // Load status image if available
            if (status.imageUrl != null) {
                imgStatus.visibility = View.VISIBLE
                Glide.with(itemView.context).load(status.imageUrl).into(imgStatus)
            } else {
                imgStatus.visibility = View.GONE
            }

            // Display user information
            txtNameStatus.text = status.text // assuming status.text contains the user's name after data is loaded
            if (status.profileImageUrl != null) {
                Glide.with(itemView.context).load(status.profileImageUrl).into(imgProfileStatus)
            } else {
                imgProfileStatus.setImageResource(R.drawable.profile) // Default avatar image
            }
        }
    }
}

