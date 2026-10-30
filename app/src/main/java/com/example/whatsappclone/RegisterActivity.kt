package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsappclone.databinding.ActivityRegisterBinding
import com.example.whatsappclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()


        binding.buttonRegister.setOnClickListener {
            registerUser()
        }

        binding.textLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.editTextName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }


        binding.progressBar.visibility = android.view.View.VISIBLE


        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener


                    val user = User(userId, name, email, phone, "")


                    database.reference.child("users").child(userId).setValue(user)
                        .addOnCompleteListener { dbTask ->
                            binding.progressBar.visibility = android.view.View.GONE

                            if (dbTask.isSuccessful) {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to save user data: ${dbTask.exception?.message}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {

                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
