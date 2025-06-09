package com.example.kai_content

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.kai_content.databinding.ActivityMainBinding
import com.example.kai_content.models.profile.Profile
import com.example.kai_content.ui.home.FeedbackDialogFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home2,R.id.nav_complaint, R.id.nav_slideshow, R.id.nav_profile,
                R.id.nav_complaint_operator, R.id.nav_content_operator
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userRole = intent.getStringExtra("USER_ROLE")
        val userProfilePicture = intent.getStringExtra("USER_PROFILE_PICT")

        val profile = Profile(
            name = userName ?: "Guest",
            role = userRole ?: "user",
            profilePictureUrl = userProfilePicture ?: ""
        )

        // Navigasi berdasarkan role
        if (userRole == "operator") {
            navController.navigate(R.id.nav_complaint_operator)

            // Untuk hide menu berdasarkan role
            val menu = navView.menu
            menu.findItem(R.id.nav_home).isVisible = false
            menu.findItem(R.id.nav_home2).isVisible = false
            menu.findItem(R.id.nav_favorite).isVisible = false
            menu.findItem(R.id.nav_slideshow).isVisible = false
            menu.findItem(R.id.nav_complaint).isVisible = false
            menu.findItem(R.id.nav_complaint_operator).isVisible = true
        } else {
            navController.navigate(R.id.nav_home2)

            val menu = navView.menu
            menu.findItem(R.id.nav_home).isVisible = true
            menu.findItem(R.id.nav_complaint).isVisible = true
            menu.findItem(R.id.nav_favorite).isVisible = true
            menu.findItem(R.id.nav_profile).isVisible = true
            menu.findItem(R.id.nav_slideshow).isVisible = true
            menu.findItem(R.id.nav_complaint_operator).isVisible = false
            menu.findItem(R.id.nav_content_operator).isVisible = false
        }

        // Get the header view from the NavigationView
        val headerView: View = navView.getHeaderView(0)

        // Find the TextViews in the header view. It is possible that the id is different
        val profileImageView = headerView.findViewById<ImageView>(R.id.imageView)
        val nameTextView = headerView.findViewById<TextView>(R.id.nameTextView)
        val emailTextView = headerView.findViewById<TextView>(R.id.emailTextView)

        // Update the TextViews with the username and email
        Glide.with(this)
            .load(profile.profilePictureUrl)
            .placeholder(R.drawable.ic_profile_placeholder) // opsional, gambar default saat loading
            .error(R.drawable.error_thumbnail) // opsional, jika URL gagal diload
            .circleCrop() // opsional, jika ingin bentuk bulat
            .into(profileImageView)

        nameTextView.text = userName
        emailTextView.text = userEmail
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // Clear token from SharedPreferences
                val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.remove("auth_token")
                editor.apply()

                // Navigate to LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                // Show a toast
                Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_ratings -> {
                // Tindakan untuk action_ratings
                FeedbackDialogFragment.newInstance(30)
                    .show(supportFragmentManager, "FeedbackDialog")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
