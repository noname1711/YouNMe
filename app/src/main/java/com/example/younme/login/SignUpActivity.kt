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
import com.example.younme.adapter.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnSignUp: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        mAuth = FirebaseAuth.getInstance()
        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        edtName = findViewById(R.id.edt_name)
        btnSignUp = findViewById(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            signUp(name, email, password)
        }
    }

    private fun signUp(name: String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showPasswordDialog(name, email, mAuth.currentUser?.uid!!)
                } else {
                    Toast.makeText(this@SignUpActivity, "Error creating new account", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showPasswordDialog(name: String, email: String, uid: String) {
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

                    addUserToDatabase(name, email, uid)
                    val i = Intent(this@SignUpActivity, MainActivity::class.java)
                    startActivity(i)
                    finish()
                } else {
                    deleteUserAccount()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> deleteUserAccount() }
            .create()

        dialog.show()
    }

    private fun addUserToDatabase(name: String, email: String, uid: String) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(uid).setValue(User(name, email, uid))
    }

    private fun deleteUserAccount() {
        mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
