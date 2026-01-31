package com.kazedev.musicapp.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

// Definimos las rutas de la app como objetos para evitar errores de dedo
sealed class AppScreens(val route: String, val title: String, val icon: ImageVector) {
    object Music : AppScreens("music", "Buscar", Icons.Default.Search)
    object Battle : AppScreens("battle", "Batalla", Icons.Default.GraphicEq)
}