package com.haco.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (intent.getStringExtra("FROM_SPLASH") != "true") {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
            return
        }


        auth = Firebase.auth


        val currentUser = auth.currentUser
        if (currentUser != null) {

            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
            finish()
            return
        }


        setContentView(R.layout.activity_main)

        val mail = findViewById<EditText>(R.id.editTextText)
        val sifre = findViewById<EditText>(R.id.editTextText2)
        val kaydet = findViewById<Button>(R.id.button)
        val giris = findViewById<Button>(R.id.button2)

        kaydet.setOnClickListener {
            val emailText = mail.text.toString()
            val passwordText = sifre.text.toString()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Email ve şifre boş olamaz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener {
                    Toast.makeText(this, "Kayıt Başarılı", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, FeedActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }

        giris.setOnClickListener {
            val emailText = mail.text.toString()
            val passwordText = sifre.text.toString()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Email ve şifre boş olamaz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener {
                    Toast.makeText(this, "GİRİŞ BAŞARILI", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, FeedActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }
    }
}