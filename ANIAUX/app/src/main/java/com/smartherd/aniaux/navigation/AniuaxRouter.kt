package com.smartherd.aniaux.navigation
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.smartherd.aniaux.AuthViewModel.AuthViewModel
import com.smartherd.aniaux.screens.Login
import com.smartherd.aniaux.screens.Profile
import com.smartherd.aniaux.screens.SignUpScreen


@Composable
fun MyAppNavigation(modifier: Modifier= Modifier, authViewModel: AuthViewModel){
    val navController = rememberNavController()
    NavHost(
        navController = navController, startDestination = "Login"){
        composable("signUp"){
            SignUpScreen(modifier, navController, authViewModel)
        }
        composable("Login"){
            Login(modifier, navController, authViewModel)
        }
        composable("BottomAppBar"){
            MybottomAppBar(modifier, navController, authViewModel)
        }
        composable("profile"){
            Profile(modifier, navController, authViewModel)
        }
//        composable("home"){
//            Home(authViewModel)
//        }//
//        //
//        composable("favourite"){
//            Favourite(modifier, navController, authViewModel)
//        }
//        composable("category"){
//            Categories(modifier, navController, authViewModel)
//        }
//        composable("postCattle"){
//            PostCattle(modifier, navController, authViewModel)
//        }
//        composable(Screens.Screens.Favourite.screen) { Favourite()  }
//        composable(Screens.Screens.Categories.screen) { Categories()  }
//        composable(Screens.Screens.PostCattle.screen) { PostCattle()  }
    }



}
