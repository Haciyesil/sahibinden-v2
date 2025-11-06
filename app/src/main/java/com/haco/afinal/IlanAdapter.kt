package com.haco.afinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class IlanAdapter(private val ilanList: ArrayList<Ilan>) : RecyclerView.Adapter<IlanAdapter.IlanHolder>() {


    class IlanHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Görünüm elemanlarını tanımlama
        val imageView: ImageView = itemView.findViewById(R.id.recyclerImageView)
        val markaModelText: TextView = itemView.findViewById(R.id.recyclerMarkaModelText)
        val yilKmText: TextView = itemView.findViewById(R.id.recyclerYilKmText)
        val fiyatText: TextView = itemView.findViewById(R.id.recyclerFiyatText)
        val emailText: TextView = itemView.findViewById(R.id.recyclerEmailText)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IlanHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_row, parent, false)
        return IlanHolder(view)
    }


    override fun onBindViewHolder(holder: IlanHolder, position: Int) {
        val currentIlan = ilanList[position]


        holder.markaModelText.text = "${currentIlan.marka} ${currentIlan.model}"
        holder.yilKmText.text = "Yıl: ${currentIlan.yil} - KM: ${currentIlan.km}"
        holder.fiyatText.text = "${currentIlan.fiyat} TL"
        holder.emailText.text = "İletişim: ${currentIlan.userEmail}"


        try {
            if (currentIlan.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(currentIlan.imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            holder.imageView.setImageResource(R.drawable.ic_launcher_background)
        }


        holder.itemView.setOnClickListener {
            val fragment = IlanDetayFragment.newInstance(currentIlan)
            fragment.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, "ilanDetay")
        }

        // Uzun basma olayı - silme işlemi
        holder.itemView.setOnLongClickListener {

            android.app.AlertDialog.Builder(holder.itemView.context)
                .setTitle("İlanı Sil")
                .setMessage("Bu ilanı silmek istediğinizden emin misiniz?")
                .setPositiveButton("Evet") { dialog, _ ->
                    // Firebase'den ilanı sil
                    val db = FirebaseFirestore.getInstance()
                    db.collection("Ilanlar").document(currentIlan.id)
                        .delete()
                        .addOnSuccessListener {

                            ilanList.removeAt(position)
                            notifyItemRemoved(position)
                            Toast.makeText(holder.itemView.context, "İlan silindi", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->

                            Toast.makeText(holder.itemView.context,
                                "Silme işlemi başarısız: ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    dialog.dismiss()
                }
                .setNegativeButton("Hayır") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            true
        }
    }


    override fun getItemCount(): Int {
        return ilanList.size
    }
}