package com.example.whatsappclone.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappclone.ChatActivity
import com.example.whatsappclone.adapters.ChatListAdapter
import com.example.whatsappclone.databinding.FragmentChatsBinding
import com.example.whatsappclone.models.ChatListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatsFragment : Fragment(), ChatListAdapter.OnItemClickListener {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var chatListAdapter: ChatListAdapter
    private val chatList = ArrayList<ChatListItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()


        chatListAdapter = ChatListAdapter(requireContext(), chatList, this)
        binding.recyclerViewChats.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewChats.adapter = chatListAdapter


        loadChatList()
    }

    private fun loadChatList() {
        val currentUserId = auth.currentUser?.uid ?: return

        database.reference.child("chatList").child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatList.clear()
                    for (chatSnapshot in snapshot.children) {
                        val chatItem = chatSnapshot.getValue(ChatListItem::class.java)
                        chatItem?.let { chatList.add(it) }
                    }


                    chatList.sortByDescending { it.timestamp }

                    chatListAdapter.notifyDataSetChanged()

                    // Show empty view if list is empty
                    if (chatList.isEmpty()) {
                        binding.textEmptyChats.visibility = View.VISIBLE
                    } else {
                        binding.textEmptyChats.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onItemClick(chatItem: ChatListItem) {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra("userId", chatItem.userId)
            putExtra("userName", chatItem.userName)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
