package com.haco.afinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.squareup.picasso.Picasso

class IlanDetayFragment : DialogFragment() {
    // İlan detaylarını tutacak değişken
    private var ilan: Ilan? = null

    // Fragment oluşturulduğunda çağrılır
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tam ekran dialog stili ayarla
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    // Fragment başladığında çağrılır
    override fun onStart() {
        super.onStart()
        // Dialog penceresinin boyutlarını ayarla
        dialog?.window?.apply {
            // Tam ekran boyutlarını ayarla
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    // Fragment'ın görünümünü oluştur
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Layout dosyasını şişir (inflate)
        return inflater.inflate(R.layout.fragment_ilan_detay, container, false)
    }

    // Görünüm oluşturulduktan sonra çağrılır
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // İlan null değilse detayları göster
        ilan?.let { currentIlan ->
            // Marka ve model bilgisini ayarla
            view.findViewById<TextView>(R.id.detayMarkaModelText)?.text =
                "${currentIlan.marka} ${currentIlan.model}"

            // Yıl ve kilometre bilgisini ayarla
            view.findViewById<TextView>(R.id.detayYilKmText)?.text =
                "Yıl: ${currentIlan.yil} - KM: ${currentIlan.km}"

            // Fiyat bilgisini ayarla
            view.findViewById<TextView>(R.id.detayFiyatText)?.text =
                "${currentIlan.fiyat} TL"

            // Resmi Picasso ile yükle
            view.findViewById<ImageView>(R.id.detayImageView)?.let { imageView ->
                Picasso.get()
                    .load(currentIlan.imageUrl)  // Resim URL'ini yükle
                    .into(imageView)             // ImageView'a yerleştir
            }
        }

        // Kapatma butonuna tıklama olayı
        view.findViewById<View>(R.id.closeButton)?.setOnClickListener {
            dismiss()  // Fragment'ı kapat
        }
    }

    // Companion object - static metodlar ve sabitler için
    companion object {
        // Yeni bir fragment örneği oluştur
        fun newInstance(ilan: Ilan) = IlanDetayFragment().apply {
            this.ilan = ilan  // İlan bilgisini ata
        }
    }
}