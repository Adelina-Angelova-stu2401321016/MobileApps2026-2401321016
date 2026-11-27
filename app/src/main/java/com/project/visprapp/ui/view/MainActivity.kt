package com.project.visprapp.ui.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
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
import com.project.visprapp.R
import com.project.visprapp.data.repository.UserRepository
import com.project.visprapp.databinding.ActivityMainBinding
import com.project.visprapp.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(applicationContext)

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
                R.id.nav_login -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                R.id.nav_logout -> {
                    lifecycleScope.launch {
                        userRepository.logout()
                    }
                }
                R.id.nav_clear_log -> {
                    viewModel.clearLog()
                }
                R.id.nav_home -> {
                    // Navigate to Home View
                }
                R.id.nav_settings -> {
                    // Navigate to Settings View
                }
                R.id.nav_about -> {
                    // Navigate to About View
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
