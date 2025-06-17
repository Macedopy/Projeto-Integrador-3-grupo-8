package com.example.geowarning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geowarning.User.User
import com.example.geowarning.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_user) // usa o XML de cadastro

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (password != confirmPassword) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(email, password)
            createUser(user)
        }
    }

    private fun createUser(user: User) {
        val service = ApiClient.retrofit
        val postCall = service.createUser(user)

        postCall.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Usuário cadastrado com sucesso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                    setContentView(R.layout.authentication)
                } else {
                    Toast.makeText(this@RegisterActivity, "Erro ao cadastrar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}