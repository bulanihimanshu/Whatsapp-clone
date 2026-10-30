package com.example.whatsappclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.databinding.ItemChatBinding
import com.example.whatsappclone.models.ChatListItem
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(
    private val context: Context,
    private val chatList: List<ChatListItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(chatItem: ChatListItem)
    }

    inner class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(chatList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = chatList[position]

        holder.binding.textName.text = chatItem.userName
        holder.binding.textLastMessage.text = chatItem.lastMessage
        holder.binding.textTime.text = formatTime(chatItem.timestamp)


        if (chatItem.profileImage.isNotEmpty()) {
            Glide.with(context)
                .load(chatItem.profileImage)
                .placeholder(R.drawable.default_profile)
                .into(holder.binding.imageProfile)
        } else {
            holder.binding.imageProfile.setImageResource(R.drawable.default_profile)
        }
    }

    override fun getItemCount(): Int = chatList.size

    private fun formatTime(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.timeInMillis = timestamp
        val messageDay = calendar.get(Calendar.DAY_OF_YEAR)

        return when {
            currentDay == messageDay -> {

                SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
            }
            currentDay - messageDay == 1 -> {

                "Yesterday"
            }
            else -> {

                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(timestamp)
            }
        }
    }
}
