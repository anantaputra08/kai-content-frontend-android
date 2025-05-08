package com.example.kai_content

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.kai_content.databinding.ActivityMainBinding
import com.example.kai_content.ui.home.FeedbackDialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

//        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_complaint, R.id.nav_slideshow, R.id.nav_profile,
                R.id.nav_complaint_operator, R.id.nav_content_operator
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Get the username and email from the Intent extras
        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userRole = intent.getStringExtra("USER_ROLE")

        // Navigasi berdasarkan role
        if (userRole == "operator") {
            navController.navigate(R.id.nav_complaint_operator)

            // Untuk hide menu berdasarkan role
            val menu = navView.menu
            menu.findItem(R.id.nav_slideshow).isVisible = false
            menu.findItem(R.id.nav_complaint).isVisible = false
            menu.findItem(R.id.nav_home).isVisible = false
            menu.findItem(R.id.nav_complaint_operator).isVisible = true
        } else {
            navController.navigate(R.id.nav_home) // Default fragment untuk non-operator

            // Untuk hide menu berdasarkan role
            val menu = navView.menu
            menu.findItem(R.id.nav_complaint_operator).isVisible = false
            menu.findItem(R.id.nav_content_operator).isVisible = false
        }

        // Get the header view from the NavigationView
        val headerView: View = navView.getHeaderView(0)

        // Find the TextViews in the header view. It is possible that the id is different
        val nameTextView = headerView.findViewById<TextView>(R.id.nameTextView)
        val emailTextView = headerView.findViewById<TextView>(R.id.emailTextView)

        // Update the TextViews with the username and email
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
            R.id.action_settings -> {
                // Tindakan untuk action_settings
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
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
