package com.smartherd.aniaux.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartherd.aniaux.AuthViewModel.AuthState
import com.smartherd.aniaux.AuthViewModel.AuthViewModel
import com.smartherd.aniaux.bottomsheetScreens.PostCattle
import com.smartherd.aniaux.dataViewModel.DataViewModel
import com.smartherd.aniaux.dataViewModel.saveUserPreferences
import com.smartherd.aniaux.models.Product
import com.smartherd.aniaux.screens.BidList
import com.smartherd.aniaux.screens.Favourite
import com.smartherd.aniaux.screens.Home
import com.smartherd.aniaux.screens.Screens
import com.smartherd.aniaux.ui.theme.grey
import com.smartherd.aniaux.utils.Constants


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MybottomAppBar(modifier: Modifier, navController: NavController, authViewModel: AuthViewModel){
    val context = LocalContext.current.applicationContext
    val sharedPreferences = context.getSharedPreferences(Constants.ANIAUX_PREFERENCES, Context.MODE_PRIVATE)
    val LoggedInUserName = sharedPreferences.getString(Constants.LOGGED_IN_USERNAME, "") ?: ""
    val Role = sharedPreferences.getString(Constants.ROLE,"")?:""
    val authState = authViewModel.authState.observeAsState()
    val navigationController = rememberNavController()
    //Toast.makeText(context, "Role: $Role    and $LoggedInUserName", Toast.LENGTH_SHORT).show()

    var isClicked by rememberSaveable { mutableStateOf(false) }
    val selected = remember {
        mutableStateOf(Icons.Default.Home)
    }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Anauthenticated -> navController.navigate("Login")
            else -> {
                // Fetch user data from Firestore
                FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .get()
                    .addOnSuccessListener { document ->
                        // Check if the document exists
                        if (document.exists()) {
                            val userName = document.getString("userName") ?: ""
                            val email = document.getString("email") ?: ""
                            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                            val userRole = document.getString("role") ?: ""
                            val picUrl = document.getString("profile_picture") ?: ""

                            // Save user details to SharedPreferences
                            saveUserPreferences(userName, email, uid, userRole, picUrl, context)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error fetching user data: ${e.message}")
                    }
            }
        }
    }
    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = grey) {
                IconButton(onClick = {
                    selected.value = Icons.Default.Home
                    navigationController.navigate(Screens.Screens.Home.screen){
                        popUpTo(0)
                    }
                },
                    modifier =Modifier.weight(1.5f)) {
                    Icon(Icons.Default.Home,
                        contentDescription = "Home",
                        Modifier.size(25.dp),
                        tint = if (selected.value == Icons.Default.Home) Color.Black   else MaterialTheme.colorScheme.primary )
                }
                //Categories
                IconButton(onClick = {
                    selected.value = Icons.Filled.List   
                    navigationController.navigate(Screens.Screens.Categories.screen){
                        popUpTo(0)
                    }
                },
                    modifier =Modifier.weight(1.5f)) {
                    Icon(Icons.Filled.List,
                            // painter = painterResource(R.drawable.sort_icon),
                        contentDescription = "Sort Icon",
                        modifier = Modifier
                            .size(25.dp),
//                            .clickable { isClicked = !isClicked },

                        tint = if(selected.value == Icons.Filled.List) Color.Black else  MaterialTheme.colorScheme.primary
                    )

                }

                //floating btn
                Box(modifier = Modifier
                    .weight(2.5f)
                    .padding(16.dp),
                    contentAlignment = Alignment.Center
                ){
                    FloatingActionButton(onClick = { showBottomSheet= true }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = grey)
                    }

                }

                IconButton(onClick = {
                    selected.value = Icons.Default.Favorite
                    navigationController.navigate(Screens.Screens.Favourite.screen){
                        popUpTo(0)
                    }
                },
                    modifier =Modifier.weight(2f)) {
                    Icon(Icons.Default.Favorite,
                        contentDescription = "Favourite",
                        Modifier.size(25.dp),
                        tint = if (selected.value == Icons.Default.Favorite) Color.Black   else MaterialTheme.colorScheme.primary )
                }

                //profile
                IconButton(onClick = {
                    selected.value = Icons.Default.AccountCircle
                    navController.navigate("profile"){
                        popUpTo(0)
                    }
                },
                    modifier =Modifier.weight(2f)) {
                    Icon(Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        Modifier.size(25.dp),
                        tint = if (selected.value == Icons.Default.AccountCircle) Color.Black   else MaterialTheme.colorScheme.primary )
                }
            }
        }
    ) {
            paddingValues ->
        NavHost(navController = navigationController,
            startDestination = Screens.Screens.Home.screen,
            modifier = Modifier.padding(paddingValues)
        ){


           composable(Screens.Screens.Home.screen){ Home(authViewModel)}
            composable(Screens.Screens.Favourite.screen) { Favourite(dataViewModel = DataViewModel(), authViewModel)  }
            composable(Screens.Screens.Categories.screen) { BidList(dataViewModel = DataViewModel(),
                authViewModel = authViewModel, navController = navController)  }
           // composable(Screens.Screens.Profile.screen) { Profile(modifier, navController, authViewModel)  }

            composable(Screens.Screens.PostCattle.screen) { PostCattle(authViewModel, navController)  }
            //composable(Screens.Screens.Login.screen) { Login(modifier,navigationController,authViewModel)  }
           // composable(Screens.Screens.SignUpScreen.screen) { SignUpScreen(modifier,navigationController,authViewModel)  }


        }
    }
    if(showBottomSheet){
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                if(Role == "Seller"){
                    BottomSheetItems(icon = Icons.Default.Add, title = "add a cattle to sell", ){
                        showBottomSheet = false
                        navigationController.navigate(Screens.Screens.PostCattle.screen){ popUpTo(0) }
                    }
                }
                else{
                    BottomSheetItems(icon = Icons.Default.Face, title = "Become a Seller to Enjoy our deals", ){
                        showBottomSheet = false
                       // navigationController.navigate(Screens.Screens.PostCattle.screen){ popUpTo(0) }
                    }
                }

                BottomSheetItems(icon = Icons.Default.ExitToApp, title = "Logout", ){
                    showBottomSheet = false
                   // authViewModel.LogoutUser()
                }
            }
        }
    }



}

@Composable
fun BottomSheetItems(icon:ImageVector, title: String, onClick: () -> Unit){
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.clickable { onClick() })
    {
        Icon(icon, contentDescription = null, tint = Color.Black)
        Text(text = title, color = Color.Black, fontSize = 22.sp )
    }

}

