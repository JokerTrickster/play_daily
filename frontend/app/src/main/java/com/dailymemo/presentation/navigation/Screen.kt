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
        object Edit : Memory("memory/edit")
    }

    sealed class Profile(route: String) : Screen(route) {
        object Edit : Profile("profile/edit")
        object Participants : Profile("profile/participants")
    }

    sealed class Room(route: String) : Screen(route) {
        object Discovery : Room("room/discovery")
        object Detail : Room("room/detail")
    }

    object Collaboration : Screen("collaboration")
}
