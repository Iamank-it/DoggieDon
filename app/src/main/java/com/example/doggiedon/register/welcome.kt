package com.example.doggiedon.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.doggiedon.MainActivity
import com.example.doggiedon.R
import kotlinx.coroutines.launch


class Welcome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val googleAuthClient = GoogleAuthClient(this)
        lifecycleScope.launch {
            val signinvalue = googleAuthClient.signIn()
            if(signinvalue){
                val intent = Intent(this@Welcome, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }
        val btn1 = findViewById<Button>(R.id.signin)
        btn1.setOnClickListener {
            lifecycleScope.launch {
                val signinvalue = googleAuthClient.signIn()
                if(signinvalue){
                    val intent = Intent(this@Welcome, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }
        }
    }
}
