package com.example.aplicacionfinalmultimedia

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
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
import com.example.aplicacionfinalmultimedia.MainActivity.Companion.recipeList
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

        intent

        recipeTitle = findViewById(R.id.recipeTitle)
        addPhotoButton = findViewById(R.id.addPhotoButton)
        addAudioButton = findViewById(R.id.addAudioButton)
        saveButton = findViewById(R.id.saveButton)

        // Añadir foto
        addPhotoButton.setOnClickListener {
            if(checkPermission(android.Manifest.permission.CAMERA, PERMISSION_CODE)) openCamera()
        }

        // Añadir audio
        addAudioButton.setOnClickListener {
            if(checkPermission(android.Manifest.permission.RECORD_AUDIO, PERMISSION_CODE)) recordAudio()
        }

        // Guardar receta
        saveButton.setOnClickListener {
            val title = recipeTitle.text.toString()
            if (title.isNotEmpty()) {
                try {
                    val newRecipe = Recipe(recipeList.size + 1, title, photoPath, audioPath)
                    recipeList.add(newRecipe)
                    finish()
                }catch (e:Exception){

                }

            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) :Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            return false
        }else{
            return true
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
                    // La URI del audio grabado
                    val audioUri = data?.data
                    // Obtener la ruta real del archivo usando el ContentResolver
                    audioPath = audioUri?.let { getRealPathFromURI(it) }
                }
            }
        }
    }
    private fun getRealPathFromURI(uri: Uri): String? {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA)
            cursor = contentResolver.query(uri, projection, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            cursor?.moveToFirst()
            return cursor?.getString(columnIndex ?: -1)
        } finally {
            cursor?.close()
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
        const val PERMISSION_CODE=100
        const val CAMERA_REQUEST_CODE = 101
        const val AUDIO_REQUEST_CODE = 103
    }
}