package com.efrei.nanoorbit.ui.navigation

sealed class Routes(val route: String) {
    object Dashboard : Routes("dashboard")
    object Detail : Routes("detail/{satelliteId}") {
        fun createRoute(satelliteId: String) = "detail/$satelliteId"
    }
    object Planning : Routes("planning")
    object Map : Routes("map")
}