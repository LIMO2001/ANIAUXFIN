package com.smartherd.aniaux.screens

class Screens {
    sealed class Screens(val screen: String) {
        data object Home:Screens("Home")
        data object Categories:Screens("Categories")
        data object Favourite:Screens("Favourite")
        data object Profile:Screens("Profile")
        data object Login:Screens("Login")
        data object SignUpScreen:Screens("Register")
        data object BottomAppBar:Screens("Register")
        data object PostCattle:Screens("post")



    }
}