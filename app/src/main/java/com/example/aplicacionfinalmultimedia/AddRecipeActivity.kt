package com.example.aplicacionfinalmultimedia

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aplicacionfinalmultimedia.Model.Recipe
import java.io.File
import java.io.FileOutputStream

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var recipeTitle: EditText
    private lateinit var addPhotoButton: Button
    private lateinit var addAudioButton: Button
    private lateinit var saveButton: Button

    private var photoPath: String? = null
    private var audioPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        recipeTitle = findViewById(R.id.recipeTitle)
        addPhotoButton = findViewById(R.id.addPhotoButton)
        addAudioButton = findViewById(R.id.addAudioButton)
        saveButton = findViewById(R.id.saveButton)

        // Añadir foto
        addPhotoButton.setOnClickListener {
            checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)
            openCamera()
        }

        // Añadir audio
        addAudioButton.setOnClickListener {
            checkPermission(Manifest.permission.RECORD_AUDIO, AUDIO_PERMISSION_CODE)
            recordAudio()
        }

        // Guardar receta
        saveButton.setOnClickListener {
            val title = recipeTitle.text.toString()
            if (title.isNotEmpty()) {
                val newRecipe = Recipe(recipeList.size + 1, title, photoPath, audioPath)
                val intent = Intent()
                intent.putExtra("newRecipe", newRecipe)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun recordAudio() {
        val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        startActivityForResult(intent, AUDIO_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    photoPath = saveImage(bitmap)
                }
                AUDIO_REQUEST_CODE -> {
                    audioPath = data?.data?.path
                }
            }
        }
    }

    private fun saveImage(bitmap: Bitmap): String {
        val fileName = "recipe_image_${System.currentTimeMillis()}.jpg"
        val dir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Recipes")
        dir.mkdirs()
        val file = File(dir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file.absolutePath
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
        private const val AUDIO_PERMISSION_CODE = 102
        private const val AUDIO_REQUEST_CODE = 103
    }
}