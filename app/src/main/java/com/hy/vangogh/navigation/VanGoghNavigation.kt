package com.hy.vangogh.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hy.vangogh.ui.screens.HomeScreen
import com.hy.vangogh.ui.screens.ImageEditScreen
import com.hy.vangogh.presentation.viewmodel.ImageEditViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ImageEdit : Screen("image_edit")
}

@Composable
fun VanGoghNavigation(
    navController: NavHostController = rememberNavController()
) {
    val imageEditViewModel: ImageEditViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onProjectSelected = { project ->
                    imageEditViewModel.loadProject(project)
                    navController.navigate(Screen.ImageEdit.route)
                }
            )
        }
        
        composable(Screen.ImageEdit.route) {
            ImageEditScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                viewModel = imageEditViewModel
            )
        }
    }
}
