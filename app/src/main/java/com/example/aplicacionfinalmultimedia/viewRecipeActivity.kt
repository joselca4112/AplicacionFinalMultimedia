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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        val recipe = intent.getSerializableExtra("recipe") as Recipe

        // Mostrar título
        findViewById<TextView>(R.id.titleTextView).text = recipe.title

        // Mostrar foto
        recipe.photoPath?.let {
            Glide.with(this).load(it).into(findViewById(R.id.imageView))
        }

        // Reproducir audio
        recipe.audioPath?.let {
            findViewById<Button>(R.id.playButton).setOnClickListener {
                playAudio(it)
            }
        }
    }

    private fun playAudio(path: String) {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}