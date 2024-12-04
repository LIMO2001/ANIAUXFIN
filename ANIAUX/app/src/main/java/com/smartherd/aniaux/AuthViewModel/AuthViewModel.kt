package com.smartherd.aniaux.AuthViewModel

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.smartherd.aniaux.models.User
import com.smartherd.aniaux.utils.Constants
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {
    private val mFirestore = FirebaseFirestore.getInstance()
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    

    private val _authState =  MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    init {
        checkAuthStatus()
    }

    fun checkAuthStatus(){
        if(auth.currentUser == null){
            _authState.value = AuthState.Anauthenticated

        }else{
            _authState.value = AuthState.Authenticated
        }
    }
    fun loginUser(email: String, password: String){
        if (email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or Password cant Be Empty")
            return
        }
        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong ")
                }
            }
    }
    fun SignUpUser(email: String, password: String, userName: String, userRole: String){
        if (email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or Password cant Be Empty")
            return
        }
        if (password.length < 6){
            _authState.value = AuthState.Error("Password should be at least 6 characters")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){

                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    // Initialize the user data
                    val userInfo = User(
                        id = userId,
                        userName = userName,
                        email = email,
                        role = userRole,
                        profileCompleted = 0
                    )
                    // Save user data in Firestore
                    mFirestore.collection(Constants.USERS)
                        .document(userId)
                        .set(userInfo)
                        .addOnSuccessListener {
                            _authState.value = AuthState.Authenticated
                        }
                        .addOnFailureListener { e ->
                            _authState.value = AuthState.Error("Failed to save user profile: ${e.message}")
                        }

                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong ")
                }
            }
    }
    fun LogoutUser(){
        auth.signOut()
        _authState.value = AuthState.Anauthenticated
    }



}
sealed class AuthState{
    object Authenticated: AuthState()
    object Anauthenticated: AuthState()
    object Loading: AuthState()
    data class Error (val message: String) : AuthState()
}