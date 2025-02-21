package com.example.aplicacionfinalmultimedia

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.aplicacionfinalmultimedia.Model.Recipe
import android.widget.MediaController


class ViewRecipeActivity : AppCompatActivity() {

    private lateinit var receta:Recipe

    private var mediaPlayer: MediaPlayer? = null
    private var mediaController: MediaController? = null

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
            findViewById<Button>(R.id.audioView).setOnClickListener {
                playAudio()
            }
        }
    }

    private fun playAudio() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer?.setDataSource(receta.audioPath)
                mediaPlayer?.prepare()

                // Inicializamos el MediaController
                mediaController = MediaController(this)
                mediaController?.setMediaPlayer(object : MediaController.MediaPlayerControl {
                    override fun start() {
                        mediaPlayer?.start()
                    }

                    override fun pause() {
                        mediaPlayer?.pause()
                    }

                    override fun getDuration(): Int {
                        return mediaPlayer?.duration ?: 0
                    }

                    override fun getCurrentPosition(): Int {
                        return mediaPlayer?.currentPosition ?: 0
                    }

                    override fun seekTo(pos: Int) {
                        mediaPlayer?.seekTo(pos)
                    }

                    override fun isPlaying(): Boolean {
                        return mediaPlayer?.isPlaying == true
                    }

                    override fun getBufferPercentage(): Int {
                        return 0 // Puedes implementarlo si deseas gestionar el buffer
                    }

                    override fun canPause(): Boolean {
                        return true
                    }

                    override fun canSeekBackward(): Boolean {
                        return true
                    }

                    override fun canSeekForward(): Boolean {
                        return true
                    }
                    // Implementación del método getAudioSessionId
                    override fun getAudioSessionId(): Int {
                        return mediaPlayer?.audioSessionId ?: 0
                    }
                })
                mediaController?.setAnchorView(findViewById(R.id.audioView)) // Tu layout que contendrá el MediaController
                mediaController?.show() // Muestra los controles

                mediaPlayer?.start()

            } catch (e: Exception) {
                Log.e("Error", "Audio no encontrado")
            }
        } else if (!mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
            mediaController?.show() // Mostrar controles si no están visibles
        }
    }
}