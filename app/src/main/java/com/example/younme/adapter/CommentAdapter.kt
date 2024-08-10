package com.example.younme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.younme.R
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(private val context: Context, private val comments: List<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCommentProfile: CircleImageView = itemView.findViewById(R.id.img_commentProfile)
        val txtCommentUserName: TextView = itemView.findViewById(R.id.txt_commentUserName)
        val txtCommentText: TextView = itemView.findViewById(R.id.txt_commentText)
        val txtCommentTime: TextView = itemView.findViewById(R.id.txt_commentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        Glide.with(context)
            .load(comment.userProfileImageUrl)
            .into(holder.imgCommentProfile)

        holder.txtCommentUserName.text = comment.userName
        holder.txtCommentText.text = comment.textComment

        comment.timestamp?.let {
            holder.txtCommentTime.text = getTimeAgo(it)
        }
    }

    override fun getItemCount(): Int = comments.size

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val timeDiff = now - timestamp
        val seconds = timeDiff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val months = days / 30
        val years = days / 365

        val yearString = context.getString(R.string.year)
        val monthString = context.getString(R.string.month)
        val dayString = context.getString(R.string.day)
        val hourString = context.getString(R.string.hour)
        val minuteString = context.getString(R.string.minute)
        val secondString = context.getString(R.string.second)
        val nowString = context.getString(R.string.now)

        return when {
            years > 0 -> "$years $yearString"
            months > 0 -> "$months $monthString"
            days > 0 -> "$days $dayString"
            hours > 0 -> "$hours $hourString"
            minutes > 0 -> "$minutes $minuteString"
            seconds > 0 -> "$seconds $secondString"
            else -> nowString
        }
    }
}
