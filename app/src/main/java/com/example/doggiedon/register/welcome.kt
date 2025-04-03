package com.example.doggiedon.register

import android.app.Activity
import android.os.Binder
import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.doggiedon.R
import com.example.doggiedon.databinding.ActivityWelcomeBinding

class welcome : AppCompatActivity() {

    private lateinit var binding : ActivityWelcomeBinding

    var action = Action.DEFAULT


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWelcomeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        runAction()
    }

    private fun runAction() {

        if (action.equals(Action.LOGIN) ) {
            Log.d("TAG","Message")
            binding.footer.visibility = View.VISIBLE
            binding.heading.visibility = View.VISIBLE
            binding.intro.visibility = View.VISIBLE
            binding.loginbutton.visibility = View.VISIBLE
            binding.registerbutton.visibility = View.VISIBLE
            binding.newusertext.visibility = View.VISIBLE
            binding.username.visibility = View.VISIBLE
            binding.password.visibility = View.VISIBLE
            binding.register.visibility = View.GONE
            binding.registeremail.visibility = View.GONE
            binding.registerphone.visibility = View.GONE
            binding.registername.visibility = View.GONE
            binding.registerpassword.visibility = View.GONE
            binding.pfp.visibility = View.GONE
        }
        if (action == Action.REGISTER) {
            binding.footer.visibility = View.VISIBLE
            binding.heading.visibility = View.VISIBLE
            binding.intro.visibility = View.VISIBLE
            binding.loginbutton.visibility = View.VISIBLE
            binding.register.visibility = View.VISIBLE
            binding.registeremail.visibility = View.VISIBLE
            binding.registerphone.visibility = View.VISIBLE
            binding.registername.visibility = View.VISIBLE
            binding.registerpassword.visibility = View.VISIBLE
            binding.newusertext.visibility = View.GONE
            binding.registerbutton.visibility = View.GONE
            binding.pfp.visibility = View.GONE
            binding.username.visibility = View.GONE
            binding.password.visibility = View.GONE
        }
        if (action.equals(Action.DEFAULT)) {
            Log.d("TAG","Default")
            binding.pfp.visibility = View.VISIBLE
            binding.footer.visibility = View.VISIBLE
            binding.heading.visibility = View.VISIBLE
            binding.intro.visibility = View.VISIBLE
            binding.loginbutton.visibility = View.VISIBLE
            binding.registerbutton.visibility = View.VISIBLE
            binding.newusertext.visibility = View.VISIBLE
            binding.username.visibility = View.GONE
            binding.password.visibility = View.GONE
            binding.register.visibility = View.GONE
            binding.registeremail.visibility = View.GONE
            binding.registerphone.visibility = View.GONE
            binding.registername.visibility = View.GONE
            binding.registerpassword.visibility = View.GONE
        }
        binding.loginbutton.setOnClickListener {
            action = Action.LOGIN
            runAction()
        }
        binding.registerbutton.setOnClickListener {
            action = Action.REGISTER
            runAction()
        }
        binding.register.setOnClickListener {
            action = Action.DEFAULT
            runAction()
        }
    }
}
enum class Action{
    LOGIN,REGISTER,DEFAULT
}