package com.example.aplicacionfinalmultimedia

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aplicacionfinalmultimedia.Model.Recipe

class ViewRecipeActivity : AppCompatActivity() {

    private lateinit var receta:Recipe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        receta = intent.getSerializableExtra("recipe") as Recipe

        // Mostrar título
        findViewById<TextView>(R.id.titleTextView).text = receta.title

        // Mostrar foto
        receta.photoPath?.let {
            Glide.with(this).load(it).into(findViewById(R.id.imageView))
        }

        // Reproducir audio
        receta.audioPath?.let {
            findViewById<Button>(R.id.playButton).setOnClickListener {
                playAudio()
            }
        }
    }

    private fun playAudio() {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(receta.audioPath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}