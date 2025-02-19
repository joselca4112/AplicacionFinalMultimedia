package com.example.aplicacionfinalmultimedia.Model

import java.io.Serializable


data  class Recipe(
    val id: Int,
    val title: String,
    val photoPath: String? = null, // Ruta de la foto
    val audioPath: String? = null   // Ruta del audio
) :Serializable
