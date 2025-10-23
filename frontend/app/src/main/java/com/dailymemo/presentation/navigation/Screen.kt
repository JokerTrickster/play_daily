package com.dailymemo.presentation.navigation

sealed class Screen(val route: String) {
    sealed class Auth(route: String) : Screen(route) {
        object Login : Auth("login")
        object Signup : Auth("signup")
    }

    sealed class Main(route: String) : Screen(route) {
        object Map : Main("map")
        object List : Main("list")
        object Timeline : Main("timeline")
    }

    sealed class Memory(route: String) : Screen(route) {
        object Create : Memory("memory/create")
        object Detail : Memory("memory/detail")
    }

    sealed class Profile(route: String) : Screen(route) {
        object Edit : Profile("profile/edit")
    }

    object Collaboration : Screen("collaboration")
}
