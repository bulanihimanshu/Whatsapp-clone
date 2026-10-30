package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappclone.adapters.ContactAdapter
import com.example.whatsappclone.databinding.ActivityContactsBinding
import com.example.whatsappclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactsBinding
    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")
    private val users = ArrayList<User>()
    private val adapter = ContactAdapter(users)
    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(R.string.select_contact)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewContacts.adapter = adapter
        binding.buttonRetry.setOnClickListener { loadContacts() }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }
        loadContacts()
    }

    private fun loadContacts() {
        val currentUid = auth.currentUser?.uid ?: return
        removeListener()
        binding.progressBar.visibility = View.VISIBLE
        binding.textState.visibility = View.GONE
        binding.buttonRetry.visibility = View.GONE
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    if (uid == currentUid) continue
                    val user = child.getValue(User::class.java) ?: continue
                    // Use the database key, which is also the chat recipient UID.
                    users.add(user.copy(id = uid))
                }
                users.sortBy { it.name.lowercase(java.util.Locale.getDefault()) }
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
                binding.textState.setText(R.string.no_contacts)
                binding.textState.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                users.clear()
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
                binding.textState.setText(R.string.contacts_load_failed)
                binding.textState.visibility = View.VISIBLE
                binding.buttonRetry.visibility = View.VISIBLE
            }
        }
        usersRef.addValueEventListener(listener!!)
    }

    private fun removeListener() {
        listener?.let { usersRef.removeEventListener(it) }
        listener = null
    }

    override fun onStop() {
        removeListener()
        super.onStop()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
