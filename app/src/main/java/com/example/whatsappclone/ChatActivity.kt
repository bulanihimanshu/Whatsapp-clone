package com.example.whatsappclone

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappclone.adapters.MessageAdapter
import com.example.whatsappclone.databinding.ActivityChatBinding
import com.example.whatsappclone.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var emojiPopup: EmojiPopup

    private val messageList = ArrayList<Message>()
    private var receiverId: String = ""
    private var receiverName: String = ""
    private var senderId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EmojiManager.install(GoogleEmojiProvider())

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        senderId = auth.currentUser?.uid ?: ""
        if (senderId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        receiverId = intent.getStringExtra("userId") ?: ""
        receiverName = intent.getStringExtra("userName") ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = receiverName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        messageAdapter = MessageAdapter(this@ChatActivity, messageList, senderId)
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMessages.adapter = messageAdapter

        database.reference.child("chats").keepSynced(true)
        loadMessages()


        emojiPopup = EmojiPopup(
            rootView = binding.root,
            editText = binding.editTextMessage
        )


        binding.emojiButton.setOnClickListener {
            emojiPopup.toggle()
        }

        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text?.toString()?.trim()
            if (!messageText.isNullOrEmpty()) {
                sendMessage(messageText)
                binding.editTextMessage.text?.clear()
            }
        }
    }

    private fun loadMessages() {
        val chatId = getChatId(senderId, receiverId)
        database.reference.child("chats").child(chatId).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(Message::class.java)
                        message?.let { messageList.add(it) }
                    }
                    messageAdapter.notifyDataSetChanged()
                    if (messageList.isNotEmpty()) {
                        binding.recyclerViewMessages.scrollToPosition(messageList.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sendMessage(messageText: String) {
        val timestamp = System.currentTimeMillis()
        val message = Message(senderId, messageText, timestamp)
        val chatId = getChatId(senderId, receiverId)

        val messageRef = database.reference.child("chats").child(chatId).child("messages").push()
        message.id = messageRef.key ?: ""
        messageRef.setValue(message)

        updateChatList(senderId, receiverId, message)
        updateChatList(receiverId, senderId, message)
    }

    private fun updateChatList(userId: String, otherUserId: String, lastMessage: Message) {
        val chatListRef = database.reference.child("chatList").child(userId).child(otherUserId)
        database.reference.child("users").child(otherUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").getValue(String::class.java) ?: ""
                    val userImage = snapshot.child("profileImage").getValue(String::class.java) ?: ""

                    val chatListMap = HashMap<String, Any>()
                    chatListMap["userId"] = otherUserId
                    chatListMap["userName"] = userName
                    chatListMap["profileImage"] = userImage
                    chatListMap["lastMessage"] = lastMessage.text
                    chatListMap["timestamp"] = lastMessage.timestamp

                    chatListRef.updateChildren(chatListMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "$userId1-$userId2" else "$userId2-$userId1"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
