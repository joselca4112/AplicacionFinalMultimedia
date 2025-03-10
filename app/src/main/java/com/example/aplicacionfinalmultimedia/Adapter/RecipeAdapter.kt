package com.example.aplicacionfinalmultimedia.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aplicacionfinalmultimedia.MainActivity
import com.example.aplicacionfinalmultimedia.Model.Recipe
import com.example.aplicacionfinalmultimedia.R

class RecipeAdapter(private var recipes: ArrayList<Recipe>, private val main: MainActivity, private val onItemClick: (Recipe) -> Unit) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val btnEliminar: ImageButton =itemView.findViewById(R.id.btnEliminar)

        init {
            itemView.setOnClickListener {
                onItemClick(recipes[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.titleTextView.text = recipe.title
        // Cargar imagen desde la ruta
        recipe.photoPath?.let {
            Glide.with(holder.imageView.context).load(it).into(holder.imageView)
        }
        holder.btnEliminar.setOnClickListener{
            recipes.removeAt(position)
            notifyItemRemoved(position)
            MainActivity.saveRecipesToFile(main,recipes)
        }
    }

    override fun getItemCount(): Int = recipes.size

    // Método para actualizar la lista de recetas
    fun updateRecipes(newRecipes: ArrayList<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
        MainActivity.saveRecipesToFile(main,recipes)
    }
}