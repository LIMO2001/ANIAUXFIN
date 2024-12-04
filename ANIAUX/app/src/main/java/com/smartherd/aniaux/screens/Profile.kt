package com.smartherd.aniaux.screens


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.smartherd.aniaux.AuthViewModel.AuthViewModel
import com.smartherd.aniaux.components.ProfileImage
import com.smartherd.aniaux.utils.Constants
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.smartherd.aniaux.models.Product
import coil.compose.rememberAsyncImagePainter
import com.smartherd.aniaux.models.PurchasedProducts

@Composable
fun Profile(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current.applicationContext
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var picUrl by remember { mutableStateOf("") }
    var editMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var purchasedProducts by remember { mutableStateOf<List<PurchasedProducts>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch user details and purchased products
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences(Constants.ANIAUX_PREFERENCES, Context.MODE_PRIVATE)
        userName = sharedPreferences.getString(Constants.LOGGED_IN_USERNAME, "") ?: ""
        email = sharedPreferences.getString(Constants.LOGGED_IN_USEREMAIL, "") ?: ""
        val userId = sharedPreferences.getString(Constants.LOGGED_IN_USER_ID, "") ?: ""
        picUrl = sharedPreferences.getString(Constants.PROFILE_PICTURE, "") ?: ""
        editName = userName
        editEmail = email


        val db = FirebaseFirestore.getInstance()
        // Fetch purchased products IDs
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val purchasedProductIds = document.get("purchased_products") 
                    Log.d("Profile", "Purchased products: $purchasedProductIds")
                        //   if (purchasedProductIds != null) {
                        // Fetch details for each purchased product
                        db.collection("purchased_products")
                            .whereEqualTo("userId", userId)
                            .get()
//                            .addOnSuccessListener { onsuccess ->
//                                val productsList = onsuccess.documents()
//                                purchasedProducts = productsList
//                                isLoading = false
//                            }
                            .addOnSuccessListener { result ->
                                val productsList = result.mapNotNull { document ->
                                    document.toObject(PurchasedProducts::class.java)
                                }
                                purchasedProducts = productsList
                                isLoading = false
                                Log.d("profile products", "Purchased products: $purchasedProducts")

                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(context, "Error fetching products: ${exception.message}", Toast.LENGTH_LONG).show()
                                isLoading = false
                            }
//                    } else {
//                        isLoading = false
//                    }
                } else {
                    isLoading = false
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to fetch user data: ${e.message}", Toast.LENGTH_LONG).show()
                isLoading = false
            }
    }
    Column {
        Column(modifier = modifier
            .background(Color.LightGray)
            .padding(10.dp),

            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier
                .clickable { navController.navigate("BottomAppBar") })

        }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(4.dp))
            ProfileImage(imageUrl = Uri.parse(picUrl)) { newUri ->
                updateProfilePicture(newUri)
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("User Name") },
                readOnly = !editMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editEmail,
                onValueChange = { editEmail = it },
                label = { Text("User Email") },
                readOnly = !editMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (editMode) {
                            // Save changes to Firestore
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                FirebaseFirestore.getInstance().collection("users")
                                    .document(userId)
                                    .update(
                                        mapOf(
                                            "first_name" to editName,
                                            "email" to editEmail
                                        )
                                    )
                                    .addOnSuccessListener {
                                        userName = editName
                                        email = editEmail
                                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
                                        // Revert changes in UI
                                        editName = userName
                                        editEmail = email
                                    }
                            }
                        }
                        editMode = !editMode
                    },
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(text = if (!editMode) "Edit Profile" else "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (!editMode) Icons.Default.Edit else Icons.Default.Check,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Purchased Products",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                color = Color.Blue,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (purchasedProducts.isEmpty()) {
                Text("No purchases yet.", fontSize = 16.sp, color = Color.Gray)
            } else {
                LazyColumn {
                    items(purchasedProducts) { product ->
                        PurchasedProductItem(product = product.name , image = product.image_url, price = product.price, productId = product.id,
                            onProductDeleted = { deletedProductId ->
                                // Remove the deleted product from the list
                                purchasedProducts = purchasedProducts.filter { it.id != deletedProductId }
                            })
                    }
                }
            }
        }
    }

}

@Composable
fun PurchasedProductItem(product: String, image: String, price: String, productId: String,
                         onProductDeleted: (String) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val sharedPreferences = context.getSharedPreferences(Constants.ANIAUX_PREFERENCES, Context.MODE_PRIVATE)
    val userId = sharedPreferences.getString(Constants.LOGGED_IN_USER_ID, "") ?: ""
    var isDeleting by remember { mutableStateOf(false) } // Tracks deletion progress

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White, RoundedCornerShape(8.dp)),
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = image),
            contentDescription = product,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Price: Ksh $price",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = Color.DarkGray
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier.size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp)) // Shows progress bar
            } else {
                IconButton(onClick = {
                    isDeleting = true // Start loading
                    val db = FirebaseFirestore.getInstance()
                    db.collection("purchased_products")
                        .whereEqualTo("id", productId)
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { results ->
                            if (!results.isEmpty) {
                                val documentId = results.documents[0].id
                                db.collection("purchased_products").document(documentId)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Purchased Product deleted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onProductDeleted(productId)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Failed to delete product: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to delete the product: check IDs",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error fetching product: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnCompleteListener {
                            isDeleting = false // End loading
                        }
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

private fun updateProfilePicture(uri: Uri) {

    val riversRef =
        FirebaseStorage.getInstance().getReference("profile_pictures/${FirebaseAuth.getInstance().currentUser?.uid.toString()}")
    val uploadTask = riversRef.putFile(uri)

// Register observers to listen for when the download is done or if it fails
    uploadTask.addOnFailureListener {
        // Handle unsuccessful uploads
    }.addOnSuccessListener { taskSnapshot ->
        // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
            FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .update("profile_picture", uri.toString())
        }

    }

}