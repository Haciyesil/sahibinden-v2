package com.haco.afinal

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.UUID

class ArabaIlaniActivity : AppCompatActivity() {
    // Sınıf seviyesinde değişkenler
    private lateinit var selectedImage: ImageView
    private lateinit var markaEditText: EditText
    private lateinit var modelEditText: EditText
    private lateinit var yilEditText: EditText
    private lateinit var kmEditText: EditText
    private lateinit var fiyatEditText: EditText
    private lateinit var ilanVerButton: Button
    private lateinit var resimSecButton: Button
    private var selectedUri: Uri? = null

    // Firebase servisleri
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_araba_ilani)


        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "İlan Ver"
        }

        // View elemanlarını bağlama
        selectedImage = findViewById(R.id.selectedImage)
        markaEditText = findViewById(R.id.markaEditText)
        modelEditText = findViewById(R.id.modelEditText)
        yilEditText = findViewById(R.id.yilEditText)
        kmEditText = findViewById(R.id.kmEditText)
        fiyatEditText = findViewById(R.id.fiyatEditText)
        ilanVerButton = findViewById(R.id.ilanVerButton)
        resimSecButton = findViewById(R.id.resimSecButton)


        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }


        resimSecButton.setOnClickListener {
            if (izinDurumu()) {
                galeriAc()
            } else {
                izinIste()
            }
        }


        ilanVerButton.setOnClickListener {
            ilanVerButton.isEnabled = false


            val marka = markaEditText.text.toString()
            val model = modelEditText.text.toString()
            val yil = yilEditText.text.toString()
            val km = kmEditText.text.toString()
            val fiyat = fiyatEditText.text.toString()


            if (marka.isEmpty() || model.isEmpty() || yil.isEmpty() || km.isEmpty() || fiyat.isEmpty()) {
                Toast.makeText(this, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                ilanVerButton.isEnabled = true
                return@setOnClickListener
            }


            if (selectedUri == null) {
                Toast.makeText(this, "Lütfen bir resim seçin", Toast.LENGTH_SHORT).show()
                ilanVerButton.isEnabled = true
                return@setOnClickListener
            }


            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("İlan yükleniyor...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            try {
                // Resmi Firebase Storage'a yükleme
                val uuid = UUID.randomUUID()
                val imageName = "${uuid}.jpg"
                val reference = storage.reference.child("images").child(imageName)

                // Resim yükleme işlemi
                reference.putFile(selectedUri!!).addOnSuccessListener {
                    // Resim URL'ini alma
                    reference.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()


                        val ilanMap = hashMapOf(
                            "imageUrl" to downloadUrl,
                            "marka" to marka,
                            "model" to model,
                            "yil" to yil.toInt(),
                            "km" to km.toInt(),
                            "fiyat" to fiyat.toInt(),
                            "userEmail" to auth.currentUser?.email,
                            "userId" to auth.currentUser?.uid,
                            "date" to Timestamp.now()
                        )


                        db.collection("Ilanlar").add(ilanMap)
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                Toast.makeText(this, "İlan başarıyla kaydedildi", Toast.LENGTH_LONG).show()

                                // Ana sayfaya dön
                                val intent = Intent(this, FeedActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->

                                progressDialog.dismiss()
                                Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                ilanVerButton.isEnabled = true
                            }
                    }
                }
            } catch (e: Exception) {
                // Genel hata durumu
                progressDialog.dismiss()
                Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                ilanVerButton.isEnabled = true
            }
        }
    }


    private fun izinDurumu(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }


    private val galeriLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedUri = result.data?.data
            selectedUri?.let {
                selectedImage.setImageURI(it)
                selectedImage.visibility = View.VISIBLE
            }
        }
    }


    private fun galeriAc() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galeriLauncher.launch(intent)
    }


    private fun izinIste() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            1
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                galeriAc()
            } else {
                Toast.makeText(this, "Galeri izni gerekli!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, FeedActivity::class.java)
        startActivity(intent)
        finish()
    }
}