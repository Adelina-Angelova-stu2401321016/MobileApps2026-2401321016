package com.project.visprapp.ui.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.visprapp.R
import com.project.visprapp.data.local.Session
import com.project.visprapp.data.local.SessionItem
import com.project.visprapp.data.local.User
import com.project.visprapp.data.repository.SessionRepository
import com.project.visprapp.data.repository.UserRepository
import com.project.visprapp.databinding.ActivityMainBinding
import com.project.visprapp.ui.viewmodel.CapturedWord
import com.project.visprapp.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository
    private lateinit var sessionRepository: SessionRepository
    private var activeUser: User? = null
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(applicationContext)
        sessionRepository = SessionRepository(applicationContext)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigationDrawer()
        setupClickListeners()
        observeViewModel()
        observeUserData()
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
                    // Already in MainActivity, do nothing
                }
                R.id.nav_login -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                R.id.nav_logout -> {
                    lifecycleScope.launch {
                        userRepository.logout()
                    }
                }
                R.id.nav_sessions -> {
                    startActivity(Intent(this, SessionsActivity::class.java))
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
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
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
                        menu.findItem(R.id.nav_login)?.isVisible = false
                        menu.findItem(R.id.nav_logout)?.isVisible = true
                    } else {
                        tvUsername.text = "Guest User"
                        tvEmail.text = "Tap to sign in"
                        cardAvatar.setCardBackgroundColor(Color.parseColor("#00FFCC"))
                        menu.findItem(R.id.nav_login)?.isVisible = true
                        menu.findItem(R.id.nav_logout)?.isVisible = false
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnStart.setOnClickListener {
            viewModel.startSession()
        }

        binding.btnEnd.setOnClickListener {
            viewModel.endSession()
            val currentUser = activeUser
            if (currentUser != null) {
                showSaveSessionDialog(currentUser)
            }
        }
    }

    private fun showSaveSessionDialog(user: User) {
        val capturedWords = viewModel.capturedWords.value

        MaterialAlertDialogBuilder(this)
            .setTitle("Save Session?")
            .setMessage("Would you like to save this session with ${capturedWords.size} words to your profile (${user.username})?")
            .setPositiveButton("Yes") { dialog, _ ->
                saveSessionToDatabase(user, capturedWords)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveSessionToDatabase(user: User, capturedWords: List<CapturedWord>) {
        lifecycleScope.launch {
            val newSessionId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val session = Session(
                sessionId = newSessionId,
                userId = user.userId,
                dtSaved = now,
                notes = null
            )

            val sessionItems = capturedWords.map { word ->
                SessionItem(
                    wordId = UUID.randomUUID().toString(),
                    sessionId = newSessionId,
                    dtWord = word.timestamp,
                    wordText = word.wordText,
                    notes = null
                )
            }

            sessionRepository.saveSession(session)
            if (sessionItems.isNotEmpty()) {
                sessionRepository.saveSessionItems(sessionItems)
            }

            Toast.makeText(
                this@MainActivity,
                "Session saved with ${sessionItems.size} words!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.emfProgress.collectLatest { progress ->
                        binding.emfIndicator.progress = progress
                    }
                }
                launch {
                    viewModel.wordLog.collectLatest { logText ->
                        binding.tvWordLog.text = logText
                        binding.scrollView.post {
                            binding.scrollView.fullScroll(android.view.View.FOCUS_DOWN)
                        }
                    }
                }
            }
        }
    }
}
