package com.project.visprapp.ui.view

import android.accounts.AccountManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.card.MaterialCardView
import com.project.visprapp.data.local.User
import com.project.visprapp.data.repository.UserRepository
import com.project.visprapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import java.util.UUID

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userRepository: UserRepository
    private lateinit var googleSignInClient: GoogleSignInClient

    private var currentGoogleId: String = ""
    private var currentEmail: String = ""
    private var selectedAvatarColor: String = "#00FFCC"

    private val accountPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrEmpty()) {
                currentGoogleId = accountName
                currentEmail = accountName
                val defaultDisplayName = accountName.substringBefore("@").replaceFirstChar { it.uppercase() }
                onGoogleAuthSuccess(currentGoogleId, currentEmail, defaultDisplayName)
            } else {
                Toast.makeText(this, "No Google account selected", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Sign-In canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    currentGoogleId = account.id ?: account.email ?: UUID.randomUUID().toString()
                    currentEmail = account.email ?: ""
                    val displayName = account.displayName ?: ""
                    onGoogleAuthSuccess(currentGoogleId, currentEmail, displayName)
                } else {
                    launchSystemGoogleAccountPicker()
                }
            } catch (e: ApiException) {
                // If Google Play Services returns OAuth error 12500 (unregistered app SHA-1), launch System Google Account Picker
                launchSystemGoogleAccountPicker()
            }
        } else {
            launchSystemGoogleAccountPicker()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(applicationContext)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
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
            Toast.makeText(this, "No Google accounts available on this device", Toast.LENGTH_LONG).show()
        }
    }

    private fun onGoogleAuthSuccess(googleId: String, email: String, displayName: String) {
        binding.layoutGoogleSignIn.visibility = View.GONE
        binding.layoutProfileSetup.visibility = View.VISIBLE

        binding.tvSignedInAs.text = "Signed in as: $email"

        val defaultUsername = displayName.ifEmpty {
            email.substringBefore("@").replaceFirstChar { it.uppercase() }
        }
        binding.etUsername.setText(defaultUsername)
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
            val user = User(
                userId = UUID.randomUUID().toString(),
                googleId = currentGoogleId,
                email = currentEmail,
                dtCreated = System.currentTimeMillis(),
                username = username,
                avatarColor = selectedAvatarColor
            )

            userRepository.saveUser(user)
            Toast.makeText(this@LoginActivity, "Profile saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
