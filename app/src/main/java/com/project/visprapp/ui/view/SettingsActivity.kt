package com.project.visprapp.ui.view

import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.card.MaterialCardView
import com.project.visprapp.R
import com.project.visprapp.data.local.User
import com.project.visprapp.data.repository.UserRepository
import com.project.visprapp.databinding.ActivitySettingsBinding
import com.project.visprapp.utils.ThemeManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var userRepository: UserRepository
    private lateinit var themeManager: ThemeManager
    private var activeUser: User? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager = ThemeManager(this)
        themeManager.applySavedTheme()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(applicationContext)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigationDrawer()
        observeUserData()
        setupThemeSelection()
        setupAvatarColorSelection()
        setupUserProfileEditing()
    }

    private fun setupUserProfileEditing() {
        binding.btnSaveUsername.setOnClickListener {
            val newUsername = binding.etUsernameSettings.text.toString().trim()
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = activeUser
            if (user != null) {
                lifecycleScope.launch {
                    val updatedUser = user.copy(username = newUsername)
                    userRepository.saveUser(updatedUser)
                    Toast.makeText(this@SettingsActivity, "Username updated!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupThemeSelection() {
        when (themeManager.themeMode) {
            ThemeManager.THEME_LIGHT -> binding.rbThemeLight.isChecked = true
            ThemeManager.THEME_SYSTEM -> binding.rbThemeSystem.isChecked = true
            else -> binding.rbThemeDark.isChecked = true
        }

        binding.rgThemeOptions.setOnCheckedChangeListener { _, checkedId ->
            val selectedMode = when (checkedId) {
                R.id.rbThemeLight -> ThemeManager.THEME_LIGHT
                R.id.rbThemeSystem -> ThemeManager.THEME_SYSTEM
                else -> ThemeManager.THEME_DARK
            }

            if (themeManager.themeMode != selectedMode) {
                themeManager.themeMode = selectedMode
                Toast.makeText(this, "Theme updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAvatarColorSelection() {
        val colorMap = listOf(
            binding.colorCyan to "#00FFCC",
            binding.colorOrange to "#FF5722",
            binding.colorPurple to "#9C27B0",
            binding.colorGreen to "#4CAF50",
            binding.colorPink to "#E91E63"
        )

        colorMap.forEach { (card, colorHex) ->
            card.setOnClickListener {
                val user = activeUser
                if (user != null && !user.avatarColor.equals(colorHex, ignoreCase = true)) {
                    lifecycleScope.launch {
                        val updatedUser = user.copy(avatarColor = colorHex)
                        userRepository.saveUser(updatedUser)
                        Toast.makeText(this@SettingsActivity, "Avatar color updated!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateAvatarColorCardHighlights(currentColorHex: String) {
        val colorMap = listOf(
            binding.colorCyan to "#00FFCC",
            binding.colorOrange to "#FF5722",
            binding.colorPurple to "#9C27B0",
            binding.colorGreen to "#4CAF50",
            binding.colorPink to "#E91E63"
        )

        colorMap.forEach { (card, colorHex) ->
            if (colorHex.equals(currentColorHex, ignoreCase = true)) {
                card.strokeWidth = 6
                card.strokeColor = Color.WHITE
            } else {
                card.strokeWidth = 0
            }
        }
    }

    private fun setupNavigationDrawer() {
        binding.btnMenuToggle.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
                R.id.nav_sessions -> {
                    val intent = Intent(this, SessionsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
                R.id.nav_settings -> {
                    // Already in SettingsActivity
                }
                R.id.nav_login -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                R.id.nav_logout -> {
                    lifecycleScope.launch {
                        userRepository.logout()
                    }
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun observeUserData() {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvUsername = headerView.findViewById<TextView>(R.id.tvUsername)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvEmail)
        val cardAvatar = headerView.findViewById<MaterialCardView>(R.id.cardAvatar)

        headerView.setOnClickListener {
            if (tvEmail.text == "Tap to sign in" || tvUsername.text == "Guest User") {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                startActivity(Intent(this@SettingsActivity, LoginActivity::class.java))
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userRepository.activeUser.collectLatest { user ->
                    activeUser = user
                    val menu = binding.navigationView.menu
                    if (user != null) {
                        tvUsername.text = user.username
                        tvEmail.text = user.email
                        try {
                            cardAvatar.setCardBackgroundColor(Color.parseColor(user.avatarColor))
                        } catch (e: Exception) {
                            cardAvatar.setCardBackgroundColor(Color.parseColor("#00FFCC"))
                        }

                        binding.cardUserProfile.visibility = View.VISIBLE
                        binding.cardAvatarSettings.visibility = View.VISIBLE

                        if (!binding.etUsernameSettings.hasFocus()) {
                            binding.etUsernameSettings.setText(user.username)
                        }
                        binding.tvUserEmailSettings.text = "Email: ${user.email}"
                        binding.tvUserCreatedSettings.text = "Member since: ${dateFormat.format(Date(user.dtCreated))}"

                        updateAvatarColorCardHighlights(user.avatarColor)
                        menu.findItem(R.id.nav_login)?.isVisible = false
                        menu.findItem(R.id.nav_logout)?.isVisible = true
                    } else {
                        tvUsername.text = "Guest User"
                        tvEmail.text = "Tap to sign in"
                        cardAvatar.setCardBackgroundColor(Color.parseColor("#00FFCC"))

                        binding.cardUserProfile.visibility = View.GONE
                        binding.cardAvatarSettings.visibility = View.GONE

                        menu.findItem(R.id.nav_login)?.isVisible = true
                        menu.findItem(R.id.nav_logout)?.isVisible = false
                    }
                }
            }
        }
    }
}
