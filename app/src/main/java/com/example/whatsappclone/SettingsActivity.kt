package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.whatsappclone.databinding.ActivitySettingsBinding
import com.example.whatsappclone.models.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val auth = FirebaseAuth.getInstance()
    private var profileRef: DatabaseReference? = null
    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.buttonRetry.setOnClickListener { loadProfile() }
        binding.buttonLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.logout_confirmation)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.logout) { _, _ ->
                    auth.signOut()
                    openLogin()
                }.show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            openLogin()
            return
        }
        loadProfile()
    }

    private fun loadProfile() {
        val account = auth.currentUser ?: return
        removeListener()
        binding.progressBar.visibility = View.VISIBLE
        binding.textState.visibility = View.GONE
        binding.buttonRetry.visibility = View.GONE
        binding.profileContent.visibility = View.GONE
        profileRef = FirebaseDatabase.getInstance().reference.child("users").child(account.uid)
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressBar.visibility = View.GONE
                val user = snapshot.getValue(User::class.java)
                binding.textName.text = user?.name?.takeIf { it.isNotBlank() }
                    ?: getString(R.string.not_set)
                binding.textEmail.text = account.email ?: getString(R.string.not_set)
                binding.textPhone.text = user?.phone?.takeIf { it.isNotBlank() }
                    ?: getString(R.string.not_set)
                Glide.with(this@SettingsActivity)
                    .load(user?.profileImage?.takeIf { it.isNotBlank() })
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(binding.imageProfile)
                binding.profileContent.visibility = View.VISIBLE
                binding.textState.setText(R.string.profile_missing)
                binding.textState.visibility = if (user == null) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                binding.profileContent.visibility = View.GONE
                binding.textState.setText(R.string.profile_load_failed)
                binding.textState.visibility = View.VISIBLE
                binding.buttonRetry.visibility = View.VISIBLE
            }
        }
        profileRef!!.addValueEventListener(listener!!)
    }

    private fun removeListener() {
        listener?.let { profileRef?.removeEventListener(it) }
        listener = null
        profileRef = null
    }

    private fun openLogin() {
        removeListener()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
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
