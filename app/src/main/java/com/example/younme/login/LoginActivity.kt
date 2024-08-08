package com.example.younme.login

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.younme.activity.MainActivity
import com.example.younme.R
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin : Button
    private lateinit var btnSignUp : Button

    private lateinit var mAuth: FirebaseAuth

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth= FirebaseAuth.getInstance()

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
            return
        }


        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)

        btnSignUp.setOnClickListener{
            val i = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(i)
        }

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            login(email,password)
        }
    }

    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showPasswordDialog()
                } else {
                    Toast.makeText(this@LoginActivity, "User does not exist", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_password, null)
        val edtDialogPassword = dialogView.findViewById<EditText>(R.id.edtDialogPassword)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter Password")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val enteredPassword = edtDialogPassword.text.toString()
                if (enteredPassword == "Hung dz") {
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("isLoggedIn", true)
                    editor.apply()

                    val i = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(i)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Incorrect Password", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}