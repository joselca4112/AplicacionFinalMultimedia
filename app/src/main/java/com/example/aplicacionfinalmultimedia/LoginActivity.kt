package com.example.aplicacionfinalmultimedia

import Adapter.RecipeAdapter
import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacionfinalmultimedia.Model.Recipe
import java.util.Arrays


class MainActivity : AppCompatActivity() {

    private lateinit var recipeList: ArrayList<Recipe>
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activityMain)

        // Inicializar lista de recetas
        recipeList = ArrayList()

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

    // Método para actualizar la lista de recetas
    fun updateRecipeList(newRecipe: Recipe) {
        recipeList.add(newRecipe)
        adapter.notifyDataSetChanged()
    }
}