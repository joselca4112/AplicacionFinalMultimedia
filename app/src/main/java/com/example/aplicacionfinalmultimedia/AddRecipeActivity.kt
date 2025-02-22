package com.example.aplicacionfinalmultimedia

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.aplicacionfinalmultimedia.MainActivity.Companion.recipeList
import com.example.aplicacionfinalmultimedia.Model.Recipe
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var recipeTitle: EditText
    private lateinit var addPhotoButton: Button
    private lateinit var addAudioButton: Button
    private lateinit var saveButton: Button
    private lateinit var imageView: ImageView

    private var photoPath: String? = null
    private var audioPath: String? = null

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var activityAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var activityCameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        recipeTitle = findViewById(R.id.recipeTitle)
        addPhotoButton = findViewById(R.id.addPhotoButton)
        addAudioButton = findViewById(R.id.addAudioButton)
        saveButton = findViewById(R.id.saveButton)
        imageView = findViewById(R.id.imageView)

        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        // Inicializar audiolauncher
        activityAudioLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // Guardar el audio
                val audioUri = result.data?.data
                audioUri?.let { uri ->
                    // Copiar el archivo de audio a un directorio de la app
                    val audioFile = copyAudioFileToAppDirectory(uri)
                    audioPath = audioFile?.absolutePath
                    // Avisar con un Toast
                    Toast.makeText(applicationContext, "Audio guardado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Inicializar camaralauncher
        activityCameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as Bitmap
                photoPath = saveImage(bitmap)
                // Avisar con un Toast
                Toast.makeText(applicationContext, "Imagen guardada", Toast.LENGTH_SHORT).show()
            }
        }

        // Añadir foto
        addPhotoButton.setOnClickListener {
            capturePhoto()
        }

        // Añadir audio
        addAudioButton.setOnClickListener {
            if (checkPermission(android.Manifest.permission.RECORD_AUDIO)) recordAudio()
        }

        // Guardar receta
        saveButton.setOnClickListener {
            val title = recipeTitle.text.toString()
            if (title.isNotEmpty() && !photoPath.isNullOrEmpty()) {
                try {
                    val newRecipe = Recipe(recipeList.size + 1, title, photoPath, audioPath)
                    recipeList.add(newRecipe)
                    // Avisar con un Toast
                    Toast.makeText(this, "Receta guardada con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al añadir receta", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<androidx.camera.view.PreviewView>(R.id.viewFinder).surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraX", "Error al iniciar la cámara: ${exc.message}", exc)
                Toast.makeText(this, "No se pudo iniciar la cámara: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            filesDir,
            "recipe_image_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    photoPath = photoFile.absolutePath
                    runOnUiThread {
                        imageView.setImageURI(Uri.fromFile(photoFile)) // Mostrar la foto tomada
                        // Avisar con un Toast
                        Toast.makeText(applicationContext, "Imagen guardada", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Error al tomar la foto: ${exception.message}", exception)
                }
            }
        )
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
