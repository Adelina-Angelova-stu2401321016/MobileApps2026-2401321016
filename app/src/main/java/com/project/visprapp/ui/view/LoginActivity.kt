package com.project.visprapp.ui.view

import android.accounts.AccountManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.project.visprapp.data.local.User
import com.project.visprapp.data.repository.UserRepository
import com.project.visprapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userRepository: UserRepository

    private var currentGoogleId: String = ""
    private var currentEmail: String = ""
    private var selectedAvatarColor: String = "#00FFCC"

    private val accountPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("VisprAuth", "AccountPicker resultCode=${result.resultCode}")
        if (result.resultCode == RESULT_OK && result.data != null) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            Log.d("VisprAuth", "Selected accountName='$accountName'")
            if (!accountName.isNullOrEmpty()) {
                val cleanEmail = accountName.trim().lowercase()
                currentGoogleId = cleanEmail
                currentEmail = cleanEmail
                val defaultDisplayName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                onGoogleAuthSuccess(currentGoogleId, currentEmail, defaultDisplayName)
            } else {
                Toast.makeText(this, "No Google account selected", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Sign-In canceled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(applicationContext)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnGoogleSignIn.setOnClickListener {
            performGoogleAuth()
        }

        setupColorSelection()

        binding.btnSaveProfile.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun performGoogleAuth() {
        launchSystemGoogleAccountPicker()
    }

    private fun launchSystemGoogleAccountPicker() {
        try {
            val intent = AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                true,
                null,
                null,
                null,
                null
            )
            accountPickerLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("VisprAuth", "Error launching account picker", e)
            Toast.makeText(this, "No Google accounts available on this device", Toast.LENGTH_LONG).show()
        }
    }

    private fun onGoogleAuthSuccess(googleId: String, email: String, displayName: String) {
        val normalizedEmail = email.trim().lowercase()
        currentEmail = normalizedEmail
        currentGoogleId = normalizedEmail

        lifecycleScope.launch {
            val allUsers = userRepository.getAllUsers()
            Log.d("VisprAuth", "All users in DB count=${allUsers.size}: $allUsers")
            val existingUser = userRepository.getUserByEmail(normalizedEmail)
            Log.d("VisprAuth", "Searching email '$normalizedEmail', found: $existingUser")

            if (existingUser != null) {
                val updatedUser = existingUser.copy(
                    lastLogin = System.currentTimeMillis(),
                    isLoggedIn = true
                )
                userRepository.saveUser(updatedUser)
                Toast.makeText(this@LoginActivity, "Welcome back, ${existingUser.username}!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                binding.layoutGoogleSignIn.visibility = View.GONE
                binding.layoutProfileSetup.visibility = View.VISIBLE

                binding.tvSignedInAs.text = "Signed in as: $normalizedEmail"

                val defaultUsername = displayName.ifEmpty {
                    normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                }
                binding.etUsername.setText(defaultUsername)
            }
        }
    }

    private fun setupColorSelection() {
        binding.colorCyan.setOnClickListener { selectColor("#00FFCC", binding.colorCyan) }
        binding.colorOrange.setOnClickListener { selectColor("#FF5722", binding.colorOrange) }
        binding.colorPurple.setOnClickListener { selectColor("#9C27B0", binding.colorPurple) }
        binding.colorGreen.setOnClickListener { selectColor("#4CAF50", binding.colorGreen) }
        binding.colorPink.setOnClickListener { selectColor("#E91E63", binding.colorPink) }
    }

    private fun selectColor(colorHex: String, selectedCard: MaterialCardView) {
        selectedAvatarColor = colorHex
        val cards = listOf(
            binding.colorCyan,
            binding.colorOrange,
            binding.colorPurple,
            binding.colorGreen,
            binding.colorPink
        )
        for (card in cards) {
            if (card == selectedCard) {
                card.strokeWidth = 6
                card.strokeColor = android.graphics.Color.WHITE
            } else {
                card.strokeWidth = 0
            }
        }
    }

    private fun saveUserProfile() {
        val username = binding.etUsername.text.toString().trim()
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val normalizedEmail = currentEmail.trim().lowercase()
            val now = System.currentTimeMillis()

            val existingUser = userRepository.getUserByEmail(normalizedEmail)
            Log.d("VisprAuth", "saveUserProfile: existingUser=$existingUser for '$normalizedEmail'")

            val userToSave = if (existingUser != null) {
                existingUser.copy(
                    lastLogin = now,
                    isLoggedIn = true,
                    username = username,
                    avatarColor = selectedAvatarColor
                )
            } else {
                User(
                    userId = normalizedEmail,
                    googleId = normalizedEmail,
                    email = normalizedEmail,
                    dtCreated = now,
                    lastLogin = now,
                    isLoggedIn = true,
                    username = username,
                    avatarColor = selectedAvatarColor
                )
            }

            userRepository.saveUser(userToSave)
            Log.d("VisprAuth", "Saved user to DB: $userToSave")
            Toast.makeText(this@LoginActivity, "Profile saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
