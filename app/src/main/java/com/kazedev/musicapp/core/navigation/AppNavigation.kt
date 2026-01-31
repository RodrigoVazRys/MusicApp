package com.kazedev.musicapp.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kazedev.musicapp.core.di.AppContainer
import com.kazedev.musicapp.features.music.presentation.screens.BattleScreen
import com.kazedev.musicapp.features.music.presentation.screens.MusicScreen
import com.kazedev.musicapp.features.music.presentation.viewmodel.BattleViewModelFactory
import com.kazedev.musicapp.features.music.presentation.viewmodel.MusicViewModelFactory

@Composable
fun AppNavigation(appContainer: AppContainer) {
    val navController = rememberNavController()

    val screens = listOf(
        AppScreens.Music,
        AppScreens.Battle
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreens.Music.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppScreens.Music.route) {
                val musicFactory = MusicViewModelFactory(appContainer.searchTracksUseCase)
                MusicScreen(factory = musicFactory)
            }

            composable(AppScreens.Battle.route) {
                val battleFactory = BattleViewModelFactory(
                    appContainer.getBattleUseCase,
                    appContainer.searchTracksUseCase
                )
                BattleScreen(factory = battleFactory)
            }
        }
    }
}