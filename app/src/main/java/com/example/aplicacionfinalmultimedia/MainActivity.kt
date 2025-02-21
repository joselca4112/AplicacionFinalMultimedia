package com.example.aplicacionfinalmultimedia

import android.content.Context
import com.example.aplicacionfinalmultimedia.Adapter.RecipeAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacionfinalmultimedia.AddRecipeActivity.Companion.PERMISSION_CODE
import com.example.aplicacionfinalmultimedia.Model.Recipe
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter


class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var recipeList: ArrayList<Recipe>
    }
    private lateinit var adapter: RecipeAdapter

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Permisos
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ||ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Si no se ha concedido, solicitar el permiso
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.READ_MEDIA_AUDIO)
                , PERMISSION_CODE)
        }

        // Inicializar lista de recetas
        recipeList = loadRecipesFromFile(this)

        // Configurar RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = RecipeAdapter(recipeList) { recipe ->
            startActivity(Intent(this, ViewRecipeActivity::class.java).apply {
                putExtra("recipe", recipe)
            })
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Botón para añadir receta
        findViewById<Button>(R.id.addRecipeButton).setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateRecipeList()
        saveRecipesToFile(this,recipeList)
    }

    // Método para actualizar la lista de recetas
    fun updateRecipeList() {
        adapter.notifyDataSetChanged()
    }

    private fun checkPermission(permission: String, requestCode: Int)  {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }
    // Método para guardar las recetas en un archivo JSON
    fun saveRecipesToFile(context: Context, recipeList: List<Recipe>) {
        if(recipeList.isNotEmpty()){
            val gson = Gson()
            val json = gson.toJson(recipeList) // Convertimos la lista a JSON
            val file = File(context.filesDir, "recipes.json") // Ubicación dentro de la app

            FileWriter(file).use { writer ->
                writer.write(json)
            }
        }
    }

    // Método para cargar las recetas desde el archivo JSON
    fun loadRecipesFromFile(context: Context): ArrayList<Recipe> {
        val gson = Gson()
        val file = File(context.filesDir, "recipes.json")

        if (file.exists()) {
            FileReader(file).use { reader ->
                val recipesArray = gson.fromJson(reader, Array<Recipe>::class.java)
                return ArrayList(recipesArray?.toList() ?: emptyList())
                //Si el fichero esta vacio devolvemos una lista vacia
            }
        } else {
            return ArrayList() // Si no existe, devolvemos una lista vacía
        }
    }
}