package com.example.praktikum3_andien

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        val textViewHuruf =
            findViewById<TextView>(R.id.textViewHuruf)
        val textViewKata =
            findViewById<TextView>(R.id.textViewKata)
        val huruf = intent.getStringExtra("HURUF") ?: "?"
        val kataMap = mapOf(
            "A" to "Apel 🍎",
            "B" to "Bebek 🦆",
            "C" to "Ceria 😊",
            "D" to "Domba 🐑",
            "E" to "Elang 🦅",
            "F" to "Ferari 🚗",
            "G" to "Gajah 🐘",
            "H" to "Harimau 🐯",
            "I" to "Ikan 🐟",
            "J" to "Jerapah 🦒",
            "K" to "Kuda 🐎",
            "L" to "Lumba-lumba 🐬",
            "M" to "Monyet 🐵",
            "N" to "Nanas 🍍",
            "O" to "Oren 🍊",
            "P" to "Panda 🐼",
            "Q" to "Quiz ❓",
            "R" to "Rusa 🦌",
            "S" to "Singa 🦁",
            "T" to "Tupai 🐿",
            "U" to "Unta 🐫",
            "V" to "Vespa 🛵",
            "W" to "Walrus 🦦",
            "X" to "Xylophone 🎶",
            "Y" to "Yak 🦬",
            "Z" to "Zebra 🦓"
        )
        val kata = kataMap[huruf] ?: "Tidak ditemukan"
        textViewHuruf.text = huruf
        textViewKata.text = kata
    }
}