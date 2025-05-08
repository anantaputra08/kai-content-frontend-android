package com.example.kai_content

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kai_content.api.AuthApi
import com.example.kai_content.api.RetrofitClient
import com.example.kai_content.models.register.RegisterRequest
import com.example.kai_content.models.register.RegisterResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Find views using the IDs from your layout
        val nameInput = findViewById<TextInputEditText>(R.id.editTextName)
        val emailInput = findViewById<TextInputEditText>(R.id.editTextEmail)
        val passwordInput = findViewById<TextInputEditText>(R.id.editTextPassword)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.editTextConfirmPassword)
        val phoneInput = findViewById<TextInputEditText>(R.id.editTextPhone)
        val registerButton = findViewById<MaterialButton>(R.id.buttonSignUp)
        val backToLogin = findViewById<Button>(R.id.buttonBack)

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Assuming the name is the same as email for now, or you might want to add a name field
            performRegister(name, email, password, confirmPassword, phone)
        }

        backToLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun performRegister(name: String, email: String, password: String, confirmPassword: String, phone: String) {
        val api = RetrofitClient.instance.create(AuthApi::class.java)
        val registerRequest = RegisterRequest(name, email, password, confirmPassword, phone)

        api.register(registerRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                // Log status code untuk debugging
                Log.d("RegisterActivity", "Status Code: ${response.code()}")

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null) {
                        Log.d("RegisterActivity", "Response Body: $registerResponse")
                        Toast.makeText(this@RegisterActivity, registerResponse.message, Toast.LENGTH_LONG).show()

                        // Navigasi ke LoginActivity
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // Jika respons gagal, log error body
                    val errorMessage = response.errorBody()?.string()
                    Log.e("RegisterActivity", "Error Body: $errorMessage")
                    Toast.makeText(this@RegisterActivity, "Registration failed: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e("RegisterActivity", "Failure: ${t.message}")
                Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}