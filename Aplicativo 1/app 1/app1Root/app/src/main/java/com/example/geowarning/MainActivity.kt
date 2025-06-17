package com.example.geowarning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import retrofit2.Call
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.geowarning.User.User
import com.example.geowarning.network.ApiClient
import com.example.geowarning.ui.theme.GeoWarningTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authentication)
        enableEdgeToEdge()

        var emailEditText = findViewById<EditText>(R.id.emailEditText)
        var passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton: Button = findViewById<Button>(R.id.loginButton)
        val registerButton: Button = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            processLogin(emailEditText, passwordEditText)
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }
    }

    private fun processLogin(emailEditText: EditText, passwordEditText: EditText) {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val user = User(email, password)

        val service = ApiClient.retrofit
        val getCall = service.getUser(email, password)

        getCall.enqueue(object : retrofit2.Callback<User?> {
            override fun onResponse(call: Call<User?>, response: retrofit2.Response<User?>) {
                if (response.isSuccessful && response.body() != null) {
                    // Usuário encontrado - redirecionar
                    println("Usuário encontrado! Faça o redirecionamento aqui.")
                    val intent = Intent(this@MainActivity, GaleriaActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Usuário não encontrado - criar novo
                    val postCall = service.createUser(user)
                    postCall.enqueue(object : retrofit2.Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                            if (response.isSuccessful) {
                                println("Usuário criado com sucesso!")
                                // TODO: redirecionar após cadastro
                            } else {
                                println("Erro ao criar usuário: ${response}")
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            println("Erro no POST: ${t.message}")
                        }
                    })
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                println("Erro no GET: ${t.message}")
            }
        })
    }



    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Text",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        GeoWarningTheme {
            Greeting("Android")
        }
    }}

