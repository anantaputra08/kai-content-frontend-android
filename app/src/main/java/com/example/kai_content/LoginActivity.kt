package com.example.kai_content

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kai_content.api.AuthApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.login.LoginRequest
import com.example.kai_content.models.login.LoginResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Enable edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find views
        val emailInput = findViewById<TextInputEditText>(R.id.editTextEmail)
        val passwordInput = findViewById<TextInputEditText>(R.id.editTextPassword)
        val loginButton = findViewById<MaterialButton>(R.id.buttonLogin)
        val signUpText = findViewById<TextView>(R.id.textSignUp)

        // Listener for Login Button
        loginButton.setOnClickListener {
            val email = emailInput?.text.toString().trim()
            val password = passwordInput?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password must not be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(email, password)
        }

        // Listener for Sign Up TextView
        signUpText.setOnClickListener {
            // Navigate to RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(email: String, password: String) {
        val api = RetrofitClient.instance.create(AuthApi::class.java)
        val loginRequest = LoginRequest(email, password)

        api.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Display success message
                        Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()

                        // Save token to SharedPreferences
                        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("auth_token", loginResponse.token)
                        editor.apply() // Save changes asynchronously

                        // Navigate to MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("USER_ID", loginResponse.user.id)
                        intent.putExtra("USER_NAME", loginResponse.user.name)
                        intent.putExtra("USER_EMAIL", loginResponse.user.email)
                        intent.putExtra("USER_ROLE", loginResponse.user.role)
                        intent.putExtra("USER_PROFILE_PICT", loginResponse.user.profile_picture)
                        intent.putExtra("USER_TOKEN", loginResponse.token)
                        startActivity(intent)
                        finish() // Close LoginActivity to prevent returning to it
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
