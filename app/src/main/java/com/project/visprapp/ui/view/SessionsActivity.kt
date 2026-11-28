package com.project.visprapp.ui.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.visprapp.R
import com.project.visprapp.data.local.Session
import com.project.visprapp.data.repository.SessionRepository
import com.project.visprapp.data.repository.UserRepository
import com.project.visprapp.databinding.ActivitySessionsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SessionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySessionsBinding
    private lateinit var userRepository: UserRepository
    private lateinit var sessionRepository: SessionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(applicationContext)
        sessionRepository = SessionRepository(applicationContext)

        binding.sessionsRecyclerView.layoutManager = LinearLayoutManager(this)

        setupNavigationDrawer()
        observeUserData()
        loadSessions()
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
                    // Already in SessionsActivity, do nothing
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
                startActivity(Intent(this@SessionsActivity, LoginActivity::class.java))
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

    private fun loadSessions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userRepository.activeUser.collectLatest { user ->
                    val sessionsFlow = if (user != null) {
                        sessionRepository.getSessionsForUser(user.userId)
                    } else {
                        sessionRepository.allSessions
                    }

                    sessionsFlow.collectLatest { sessions ->
                        binding.sessionsRecyclerView.adapter = SessionAdapter(
                            sessions,
                            onViewClick = { sessionToView ->
                                val intent = Intent(this@SessionsActivity, SessionDetailsActivity::class.java)
                                intent.putExtra("SESSION_ID", sessionToView.sessionId)
                                startActivity(intent)
                            },
                            onDeleteClick = { sessionToDelete ->
                                showDeleteConfirmationDialog(sessionToDelete)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(session: Session) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Session?")
            .setMessage("Are you sure you want to delete this session? This action cannot be undone.")
            .setPositiveButton("Yes") { dialog, _ ->
                lifecycleScope.launch {
                    sessionRepository.deleteSession(session)
                    Toast.makeText(this@SessionsActivity, "Session deleted", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
