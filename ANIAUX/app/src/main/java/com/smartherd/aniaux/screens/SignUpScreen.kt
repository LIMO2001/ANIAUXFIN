package com.smartherd.aniaux.screens


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.smartherd.aniaux.AuthViewModel.AuthState
import com.smartherd.aniaux.AuthViewModel.AuthViewModel
import com.smartherd.aniaux.R
import com.smartherd.aniaux.components.CustomCheckbox
import com.smartherd.aniaux.components.DynamicSelectTextField
import com.smartherd.aniaux.components.HeadingTextComponents
import com.smartherd.aniaux.components.MyTextFiedComponent
import com.smartherd.aniaux.components.PasswordTextField
import com.smartherd.aniaux.models.User

import com.smartherd.aniaux.ui.theme.ANIAUXTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SignUpScreen(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
){
    val Firebase = FirebaseAuth.getInstance()

    var context = LocalContext.current.applicationContext
    var password = rememberSaveable{ mutableStateOf("")}
    var email = rememberSaveable{mutableStateOf("")}
    var userName = rememberSaveable{mutableStateOf("")}
    var confirmPassword = rememberSaveable{mutableStateOf("")}
    var isAgreeChecked by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Select Role") }
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value ) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate("BottomAppBar")
            is AuthState.Error -> Toast.makeText(context,(authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }

    }


    Surface(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ){
            Spacer(modifier = Modifier.height(20.dp))
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
            HeadingTextComponents(value = "Sign UP")
            Spacer(modifier = Modifier.height(10.dp))
            MyTextFiedComponent(valueProperty = userName , labelValue = "User Name ", icon = Icons.Default.AccountCircle )
            Spacer(modifier = Modifier.height(10.dp))
            MyTextFiedComponent(valueProperty = email, labelValue = "Email Address",icon = Icons.Default.Email )
            Spacer(modifier = Modifier.height(10.dp))
            PasswordTextField(password= password , text = "Password")
            Spacer(modifier = Modifier.height(10.dp))
            PasswordTextField(password= confirmPassword, text = "Confirm Password")

            Spacer(modifier = Modifier.height(20.dp))
            // Add the dynamic select field for choosing Buyer or Seller
            DynamicSelectTextField(
                selectedValue = selectedRole,
                options = listOf("Buyer", "Seller"),
                label = "Role",
                onValueChangedEvent = { newRole ->
                    selectedRole = newRole
                },
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(16.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            CustomCheckbox(
                isChecked = isAgreeChecked,
                onCheckedChange = { isAgreeChecked = it },  // Update the checkbox state when it's clicked
                label = "I agree to the terms and conditions"
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Submit Button
            FilledTonalButton(
                onClick = {
                    if(password.value != confirmPassword.value){
                        Toast.makeText(context, "Password should match", Toast.LENGTH_LONG).show()
                    }else{

                        authViewModel.SignUpUser(email.value, password.value , userName.value , selectedRole )
                       // navController.navigate("Login")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(50.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color.Black, // Button background color
                    contentColor = Color.White   // Text color
                ),
                enabled = isAgreeChecked // Enable button only if the checkbox is checked
            ) {
                Text(text = "Sign Up" , style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Redirect to Login Text
            Text(
                text = "Already have an account? Login",
                color = Color.Blue,
                modifier = Modifier
                    .clickable(onClick = {
                        navController.navigate("Login")
                        //AniuaxRouter.navigateTo()
                    })
                    .padding(top = 8.dp, bottom = 30.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
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

