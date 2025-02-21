package com.example.aplicacionfinalmultimedia

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.aplicacionfinalmultimedia.MainActivity.Companion.recipeList
import com.example.aplicacionfinalmultimedia.Model.Recipe
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var recipeTitle: EditText
    private lateinit var addPhotoButton: Button
    private lateinit var addAudioButton: Button
    private lateinit var saveButton: Button

    private var photoPath: String? = null
    private var audioPath: String? = null

    private lateinit var activityAudioLauncher:
            ActivityResultLauncher<Intent>

    private lateinit var activityCameraLauncher:
            ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        recipeTitle = findViewById(R.id.recipeTitle)
        addPhotoButton = findViewById(R.id.addPhotoButton)
        addAudioButton = findViewById(R.id.addAudioButton)
        saveButton = findViewById(R.id.saveButton)

        //Inicializar audiolauncher
        activityAudioLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    //Añadir guardar el video respuesta
                    val audioUri = result.data?.data
                    audioUri?.let { uri ->
                        // Copiar el archivo de audio a un directorio de tu aplicación
                        val audioFile = copyAudioFileToAppDirectory(uri)
                        audioPath = audioFile?.absolutePath
                    }
                }
            }
        //Inicializar camaralauncher
        activityCameraLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    val bitmap = result.data?.extras?.get("data") as Bitmap
                    photoPath = saveImage(bitmap)
                }
            }

        // Añadir foto
        addPhotoButton.setOnClickListener {
            if(checkPermission(android.Manifest.permission.CAMERA)) openCamera()
        }

        // Añadir audio
        addAudioButton.setOnClickListener {
            if(checkPermission(android.Manifest.permission.RECORD_AUDIO)) recordAudio()
        }

        // Guardar receta
        saveButton.setOnClickListener {
            val title = recipeTitle.text.toString()
            if (title.isNotEmpty() && !photoPath.isNullOrEmpty()) {
                try {
                    val newRecipe = Recipe(recipeList.size + 1, title, photoPath, audioPath)
                    recipeList.add(newRecipe)
                    finish()
                }catch (e:Exception){
                    Toast.makeText(this,"Error al añadir receta",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermission(permission: String) :Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            return false
        }else{
            return true
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activityCameraLauncher.launch(intent)
    }

    private fun recordAudio() {
        val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        activityAudioLauncher.launch(intent)
    }

    private fun copyAudioFileToAppDirectory(uri: Uri): File? {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var audioFile: File? = null

        try {
            // Obtener el nombre del archivo desde la URI
            val fileName = "audio_${System.currentTimeMillis()}.mp3"
            val appDirectory = File(filesDir, "audio_files") // Crea un directorio dentro de la app
            if (!appDirectory.exists()) {
                appDirectory.mkdir()
            }

            // Crear un archivo dentro de tu directorio de aplicación
            audioFile = File(appDirectory, fileName)

            // Abrir un InputStream desde la URI del audio
            inputStream = contentResolver.openInputStream(uri)
            // Crear un OutputStream para escribir el archivo
            outputStream = FileOutputStream(audioFile)

            // Copiar los datos del InputStream al OutputStream
            inputStream?.copyTo(outputStream)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }

        return audioFile
    }

    private fun saveImage(bitmap: Bitmap): String {

        val fileName = "recipe_image_${System.currentTimeMillis()}.jpg"
        val dir = File(filesDir, "image_files")
        dir.mkdirs()
        val file = File(dir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file.absolutePath

    }
}