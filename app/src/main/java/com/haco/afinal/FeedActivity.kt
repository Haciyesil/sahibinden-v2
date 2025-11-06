package com.haco.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.util.Locale


class FeedActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ilanAdapter: IlanAdapter
    private lateinit var searchView: SearchView
    private val ilanList = ArrayList<Ilan>()
    private val filteredList = ArrayList<Ilan>()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        // RecyclerView'ı ayarlama
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        ilanAdapter = IlanAdapter(filteredList)
        recyclerView.adapter = ilanAdapter

        // Arama özelliğini ayarlama
        searchView = findViewById(R.id.searchView)
        setupSearchView()


        val menuButton = findViewById<ImageView>(R.id.menuButton)
        menuButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.bottom_nav_menu, popupMenu.menu)


            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.gonder -> {
                        val intent = Intent(this, ArabaIlaniActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.cikis -> {
                        Firebase.auth.signOut()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        getDataFromFirestore()
    }


    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterIlanlar(newText)
                return true
            }
        })
    }


    private fun filterIlanlar(query: String?) {
        filteredList.clear()
        if (query.isNullOrEmpty()) {
            filteredList.addAll(ilanList)
        } else {
            val searchText = query.lowercase(Locale.getDefault())
            for (ilan in ilanList) {
                if (ilan.marka.lowercase(Locale.getDefault()).startsWith(searchText)) {
                    filteredList.add(ilan)
                }
            }
        }
        ilanAdapter.notifyDataSetChanged()
    }

    // Firestore'dan veri alma fonksiyonu
    private fun getDataFromFirestore() {
        db.collection("Ilanlar")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    ilanList.clear()
                    filteredList.clear()

                    for (document in snapshot.documents) {
                        try {
                            val ilan = Ilan(
                                id = document.id,
                                imageUrl = document.getString("imageUrl") ?: "",
                                marka = document.getString("marka") ?: "",
                                model = document.getString("model") ?: "",
                                yil = document.getLong("yil")?.toInt() ?: 0,
                                km = document.getLong("km")?.toInt() ?: 0,
                                fiyat = document.getLong("fiyat")?.toInt() ?: 0,
                                userEmail = document.getString("userEmail") ?: ""
                            )
                            ilanList.add(ilan)
                            filteredList.add(ilan)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Veri okuma hatası: ${e.localizedMessage}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                    if (ilanList.isNotEmpty()) {
                        ilanAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, "Henüz ilan bulunmuyor", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Henüz ilan bulunmuyor", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Aktivite tekrar görünür olduğunda
    override fun onResume() {
        super.onResume()
        getDataFromFirestore()
    }
}