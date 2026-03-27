package com.easyrifa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.easyrifa.ui.screen.detail.RaffleDetailScreen
import com.easyrifa.ui.screen.draw.DrawScreen
import com.easyrifa.ui.screen.history.DrawHistoryScreen
import com.easyrifa.ui.screen.home.HomeScreen
import com.easyrifa.ui.screen.participant.AddEditParticipantScreen
import com.easyrifa.ui.screen.raffle.CreateEditRaffleScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onRaffleClick = { id ->
                    navController.navigate(Screen.RaffleDetail.createRoute(id))
                },
                onCreateRaffle = {
                    navController.navigate(Screen.CreateRaffle.route)
                }
            )
        }

        composable(Screen.CreateRaffle.route) {
            CreateEditRaffleScreen(
                raffleId = null,
                onBack = { navController.popBackStack() },
                onSaved = { id ->
                    navController.navigate(Screen.RaffleDetail.createRoute(id)) {
                        popUpTo(Screen.CreateRaffle.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.EditRaffle.ROUTE,
            arguments = listOf(navArgument("raffleId") { type = NavType.LongType })
        ) { backStack ->
            val raffleId = backStack.arguments!!.getLong("raffleId")
            CreateEditRaffleScreen(
                raffleId = raffleId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RaffleDetail.ROUTE,
            arguments = listOf(navArgument("raffleId") { type = NavType.LongType })
        ) { backStack ->
            val raffleId = backStack.arguments!!.getLong("raffleId")
            RaffleDetailScreen(
                raffleId = raffleId,
                onBack = { navController.popBackStack() },
                onEditRaffle = {
                    navController.navigate(Screen.EditRaffle.createRoute(raffleId))
                },
                onAddParticipant = {
                    navController.navigate(Screen.AddParticipant.createRoute(raffleId))
                },
                onEditParticipant = { participantId ->
                    navController.navigate(Screen.EditParticipant.createRoute(raffleId, participantId))
                },
                onStartDraw = {
                    navController.navigate(Screen.Draw.createRoute(raffleId))
                },
                onHistory = {
                    navController.navigate(Screen.DrawHistory.createRoute(raffleId))
                }
            )
        }

        composable(
            route = Screen.AddParticipant.ROUTE,
            arguments = listOf(navArgument("raffleId") { type = NavType.LongType })
        ) { backStack ->
            val raffleId = backStack.arguments!!.getLong("raffleId")
            AddEditParticipantScreen(
                raffleId = raffleId,
                participantId = null,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditParticipant.ROUTE,
            arguments = listOf(
                navArgument("raffleId") { type = NavType.LongType },
                navArgument("participantId") { type = NavType.LongType }
            )
        ) { backStack ->
            val raffleId = backStack.arguments!!.getLong("raffleId")
            val participantId = backStack.arguments!!.getLong("participantId")
            AddEditParticipantScreen(
                raffleId = raffleId,
                participantId = participantId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Draw.ROUTE,
            arguments = listOf(navArgument("raffleId") { type = NavType.LongType })
        ) { backStack ->
            val raffleId = backStack.arguments!!.getLong("raffleId")
            DrawScreen(
                raffleId = raffleId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DrawHistory.ROUTE,
            arguments = listOf(navArgument("raffleId") { type = NavType.LongType })
        ) { backStack ->
            val raffleId = backStack.arguments!!.getLong("raffleId")
            DrawHistoryScreen(
                raffleId = raffleId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
