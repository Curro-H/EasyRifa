package com.easyrifa.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")

    object CreateRaffle : Screen("raffle/create")

    object EditRaffle : Screen("raffle/edit/{raffleId}") {
        const val ROUTE = "raffle/edit/{raffleId}"
        fun createRoute(raffleId: Long) = "raffle/edit/$raffleId"
    }

    object RaffleDetail : Screen("raffle/{raffleId}/detail") {
        const val ROUTE = "raffle/{raffleId}/detail"
        fun createRoute(raffleId: Long) = "raffle/$raffleId/detail"
    }

    object AddParticipant : Screen("raffle/{raffleId}/participant/add") {
        const val ROUTE = "raffle/{raffleId}/participant/add"
        fun createRoute(raffleId: Long) = "raffle/$raffleId/participant/add"
    }

    object EditParticipant : Screen("raffle/{raffleId}/participant/{participantId}/edit") {
        const val ROUTE = "raffle/{raffleId}/participant/{participantId}/edit"
        fun createRoute(raffleId: Long, participantId: Long) =
            "raffle/$raffleId/participant/$participantId/edit"
    }

    object Draw : Screen("raffle/{raffleId}/draw") {
        const val ROUTE = "raffle/{raffleId}/draw"
        fun createRoute(raffleId: Long) = "raffle/$raffleId/draw"
    }

    object DrawHistory : Screen("raffle/{raffleId}/history") {
        const val ROUTE = "raffle/{raffleId}/history"
        fun createRoute(raffleId: Long) = "raffle/$raffleId/history"
    }
}
