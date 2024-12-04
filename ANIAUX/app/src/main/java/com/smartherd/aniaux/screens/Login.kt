package com.smartherd.aniaux.screens

import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.smartherd.aniaux.AuthViewModel.AuthViewModel
//


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource


import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.smartherd.aniaux.AuthViewModel.AuthState

import com.smartherd.aniaux.R
import com.smartherd.aniaux.components.CustomCheckbox
import com.smartherd.aniaux.components.DynamicSelectTextField
import com.smartherd.aniaux.components.HeadingTextComponents
import com.smartherd.aniaux.components.MyTextFiedComponent
import com.smartherd.aniaux.components.PasswordTextField

import com.smartherd.aniaux.ui.theme.ANIAUXTheme
@Composable
fun Login(modifier: Modifier, navController: NavController , authViewModel: AuthViewModel){
    val context = LocalContext.current.applicationContext
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value ) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate("BottomAppBar")
            is AuthState.Error -> Toast.makeText(context,(authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }
    var password = rememberSaveable{mutableStateOf("")}
    var email = rememberSaveable{mutableStateOf("")}
        var isAgreeChecked by remember { mutableStateOf(false) }
        var selectedRole by remember { mutableStateOf("Select Role") }
        Surface(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ){
                Spacer(modifier = Modifier.height(50.dp))
                Image(
                    painter = painterResource(R.drawable.iclogo),
                    contentDescription ="null",
                    contentScale = ContentScale.Crop,

                    modifier = Modifier
                        .size(150.dp)
                        .fillMaxWidth()
                        .clip(shape = CircleShape) // Optional: add rounded corners
                )
                Spacer(modifier = Modifier.height(12.dp))
                HeadingTextComponents(value = "Sign In")
                Spacer(modifier = Modifier.height(5.dp))
                Column(modifier = Modifier.heightIn(150.dp)) {
                    MyTextFiedComponent(
                        valueProperty = email,
                        labelValue = "Email Address",
                        icon = Icons.Default.Email
                    )
                    PasswordTextField(password, text = "enter Your Password")
                    Spacer(modifier = Modifier.height(15.dp))
                    //
                    // Submit Button
                    FilledTonalButton(
                        onClick = {
                            authViewModel.loginUser(email.value, password.value)
                            // Toast.makeText(context, "values Email: ${email.value} , and  Password: ${password.value}", Toast.LENGTH_LONG).show()
                            // navController.navigate("BottomAppBar")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.Black, // Button background color
                            contentColor = Color.White    // Text color
                        ),

                        // enabled = isAgreeChecked // Enable button only if the checkbox is checked
                    ) {
                        Text(text = "Submit")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Redirect to Login Text
                }
                Text(
                    text = "have No account? SignUp",
                    color = Color.Blue,
                    modifier = Modifier
                        .clickable(onClick = {
                            navController.navigate("signUp")
                            //AniuaxRouter.navigateTo()
                        })
                        .padding(top = 8.dp)
                )
                if(authState.value == AuthState.Loading){
                 //   var loading by remember { mutableStateOf(false) }
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                }

            }

        }
}
