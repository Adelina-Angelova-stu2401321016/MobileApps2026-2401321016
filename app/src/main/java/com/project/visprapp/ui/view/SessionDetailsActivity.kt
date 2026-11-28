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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.visprapp.R
import com.project.visprapp.data.local.SessionItem
import com.project.visprapp.data.repository.SessionRepository
import com.project.visprapp.data.repository.UserRepository
import com.project.visprapp.databinding.ActivitySessionDetailsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class SessionDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySessionDetailsBinding
    private lateinit var userRepository: UserRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var wordsAdapter: SessionItemAdapter

    private var currentSessionId: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySessionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository(applicationContext)
        sessionRepository = SessionRepository(applicationContext)

        currentSessionId = intent.getStringExtra("SESSION_ID") ?: ""

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupNavigationDrawer()
        observeUserData()
        setupClickListeners()
        loadSessionDetails()
    }

    private fun setupRecyclerView() {
        wordsAdapter = SessionItemAdapter(
            onSaveWordNote = { item, newNote ->
                lifecycleScope.launch {
                    sessionRepository.updateSessionItemNotes(item.wordId, newNote)
                    Toast.makeText(this@SessionDetailsActivity, "Word note updated!", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteWord = { itemToDelete ->
                showDeleteWordConfirmationDialog(itemToDelete)
            }
        )
        binding.rvSessionWords.layoutManager = LinearLayoutManager(this)
        binding.rvSessionWords.adapter = wordsAdapter
    }

    private fun showDeleteWordConfirmationDialog(item: SessionItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Word?")
            .setMessage("Are you sure you want to delete '${item.wordText}' from this session?")
            .setPositiveButton("Yes") { dialog, _ ->
                lifecycleScope.launch {
                    sessionRepository.deleteSessionItem(item)
                    Toast.makeText(this@SessionDetailsActivity, "Word deleted", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
                startActivity(Intent(this@SessionDetailsActivity, LoginActivity::class.java))
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
        binding.btnSaveNotes.setOnClickListener {
            val notesText = binding.etSessionNotes.text.toString().trim()
            lifecycleScope.launch {
                sessionRepository.updateSessionNotes(currentSessionId, notesText)
                Toast.makeText(this@SessionDetailsActivity, "Note saved!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnClearNotes.setOnClickListener {
            binding.etSessionNotes.setText("")
            lifecycleScope.launch {
                sessionRepository.updateSessionNotes(currentSessionId, "")
                Toast.makeText(this@SessionDetailsActivity, "Note cleared!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSessionDetails() {
        if (currentSessionId.isEmpty()) return

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionRepository.getSessionWithItems(currentSessionId).collectLatest { sessionWithItems ->
                    if (sessionWithItems != null) {
                        val session = sessionWithItems.session
                        val items = sessionWithItems.items

                        binding.tvSessionTime.text = "Session Time: ${dateFormat.format(Date(session.dtSaved))}"
                        binding.tvWordCount.text = "Captured Words: ${items.size}"

                        if (!binding.etSessionNotes.hasFocus()) {
                            binding.etSessionNotes.setText(session.notes ?: "")
                        }

                        wordsAdapter.submitList(items)
                        if (items.isEmpty()) {
                            binding.tvEmptyWords.visibility = View.VISIBLE
                            binding.rvSessionWords.visibility = View.GONE
                        } else {
                            binding.tvEmptyWords.visibility = View.GONE
                            binding.rvSessionWords.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}
