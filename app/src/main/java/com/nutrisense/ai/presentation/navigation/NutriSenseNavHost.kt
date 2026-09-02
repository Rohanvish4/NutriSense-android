package com.nutrisense.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nutrisense.ai.presentation.camera.CameraScreen
import com.nutrisense.ai.presentation.dashboard.DashboardScreen
import com.nutrisense.ai.presentation.history.HistoryScreen
import com.nutrisense.ai.presentation.log.LogMealScreen
import com.nutrisense.ai.presentation.results.ResultsScreen

/**
 * Main Jetpack Compose Navigation Host connecting all feature screens.
 */
@Composable
fun NutriSenseNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Dashboard,
        modifier = modifier
    ) {
        composable<Route.Dashboard> {
            DashboardScreen(
                onNavigateToCamera = {
                    navController.navigate(Route.Camera)
                },
                onNavigateToLogMeal = {
                    navController.navigate(Route.LogMeal)
                },
                onNavigateToHistory = {
                    navController.navigate(Route.History)
                },
                onMealClick = { mealId ->
                    navController.navigate(Route.Results(mealId))
                }
            )
        }

        composable<Route.Camera> {
            CameraScreen(
                onClose = {
                    navController.popBackStack()
                },
                onResultConfirmed = {
                    navController.popBackStack()
                }
            )
        }

        composable<Route.LogMeal> {
            LogMealScreen(
                onClose = {
                    navController.popBackStack()
                },
                onNavigateToScan = {
                    navController.navigate(Route.Camera) {
                        popUpTo<Route.LogMeal> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.History> {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onMealClick = { mealId ->
                    navController.navigate(Route.Results(mealId))
                }
            )
        }

        composable<Route.Results> { backStackEntry ->
            val resultsRoute = backStackEntry.toRoute<Route.Results>()
            ResultsScreen(
                mealId = resultsRoute.mealId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
