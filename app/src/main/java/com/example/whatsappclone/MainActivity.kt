package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.whatsappclone.adapters.TabAdapter
import com.example.whatsappclone.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()


        setSupportActionBar(binding.toolbar)
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }


        setupViewPager()


        checkUserAuthentication()
    }

    private fun setupViewPager() {
        val tabAdapter = TabAdapter(this)
        binding.viewPager.adapter = tabAdapter


        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "CHATS"
                1 -> tab.text = "STATUS"
                2 -> tab.text = "CALLS"
            }
        }.attach()
    }

    private fun checkUserAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser == null) {

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {

                true
            }
            R.id.menu_new_chat -> {
                startActivity(Intent(this,
                    ContactsActivity::class.java))
                true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
