package com.smartherd.aniaux.bottomsheetScreens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.smartherd.aniaux.AuthViewModel.AuthViewModel
import com.smartherd.aniaux.components.ProfileImage
import com.smartherd.aniaux.models.ProductImage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color

import java.util.UUID

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.smartherd.aniaux.components.DynamicSelectTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCattle(authViewModel: AuthViewModel, navController: NavController,) {
    // Initialize variables to track selected image URIs and product details
    val selectedImageUris = remember { mutableStateListOf<Uri>() }
    val context = LocalContext.current
    val isUploading = remember { mutableStateOf(false) }
    val productName = remember { mutableStateOf("") }
    val productDescription = remember { mutableStateOf("") }
    val productPrice = remember { mutableStateOf("") }
    val successMessage = remember { mutableStateOf<String?>(null) }
    val categories = listOf("Cow", "Sheep", "Goat")
    var selectedCategory by remember { mutableStateOf("Select Category") }
    //
    // val selectedCategory = remember { mutableStateOf(categories[0]) }
  //  var expanded by remember { mutableStateOf(false) } // Controls the dropdown menu visibility

    // Launcher to pick multiple images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris.clear()
        selectedImageUris.addAll(uris)
    }

    // UI for product upload
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Upload Product Details", modifier = Modifier.padding(bottom = 16.dp))

            // Image Upload Section with a button to select images
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("Select Images")
            }
            // Image preview section
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 16.dp)
            ) {
                items(selectedImageUris.size) { index ->
                    ProfileImage(
                        imageUrl = selectedImageUris[index],
                        onImageChangeClick = { uri ->
                            selectedImageUris[index] = uri
                        }
                    )
                }
            }

            // Product Name
            TextField(
                value = productName.value,
                onValueChange = { productName.value = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Product Description
            TextField(
                value = productDescription.value,
                onValueChange = { productDescription.value = it },
                label = { Text("Product Description") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Product Price
            TextField(
                value = productPrice.value,
                onValueChange = { productPrice.value = it },
                label = { Text("Product Price") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
            DynamicSelectTextField(
                selectedValue = selectedCategory,
                options = listOf("Cow", "Goat", "Sheep"),
                label = "Animal Type",
                onValueChangedEvent = { newRole ->
                    selectedCategory = newRole
                },
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(16.dp)
            )

            // Upload Button
            Button(
                onClick = {
                    if (!isUploading.value && productName.value.isNotEmpty() && productPrice.value.isNotEmpty() ) {
                        uploadProductImages(
                            selectedImageUris = selectedImageUris,
                            productName = productName.value,
                            productDescription = productDescription.value,
                            productPrice = productPrice.value,
                            category = selectedCategory, // Pass category to upload function
                            onUploadComplete = {
                                successMessage.value = "Product uploaded successfully!"

                                productName.value = ""
                                productPrice.value = ""
                                productDescription.value = ""
                                selectedImageUris.clear()
                                isUploading.value = false
                               // navController.navigate("BottomAppBar")
                            },
                            onUploadFailed = {
                                successMessage.value = "Failed to upload product."
                                isUploading.value = false
                            }
                        )
                        isUploading.value = true
                    }
                    else{
                        Toast.makeText(context, "Values cant be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !isUploading.value
            ) {
                Text(if (isUploading.value) "Uploading..." else "Upload Product")
            }
            // Display success or error message
            successMessage.value?.let {
                Text(
                    text = it,
                    color = if (it.contains("successfully")) Color.Green else Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
// Update the upload function to include category
fun uploadProductImages(
    selectedImageUris: List<Uri>,
    productName: String,
    productDescription: String,
    productPrice: String,
    category: String,
    onUploadComplete: () -> Unit,
    onUploadFailed: (String) -> Unit // Accept error message
) {
    if (selectedImageUris.isEmpty()) {
        onUploadFailed("No images selected for upload.")
        return
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val productId = UUID.randomUUID().toString()
    val imageUrls = mutableListOf<String>()

    val productData = mutableMapOf(
        "productId" to productId,
        "sellerId" to userId,
        "name" to productName,
        "description" to productDescription,
        "price" to productPrice,
        "category" to category,
        "image_urls" to imageUrls
    )

    // Upload images one by one
    var completedUploads = 0
    selectedImageUris.forEachIndexed { index, uri ->
        val productRef = storage.reference.child("product_images/$productId/${UUID.randomUUID()}")

        //   val productRef = storage.reference.child("product_images/${productId}_image_$index")
        productRef.putFile(uri)
            .addOnFailureListener { error ->
                onUploadFailed("Failed to upload image $index: ${error.message}")
            }
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrls.add(downloadUri.toString())
                    completedUploads++
                    if (completedUploads == selectedImageUris.size) {
                        // All images uploaded; now update Firestore
                        firestore.collection("products")
                            .document(productId)
                            .set(productData)
                            .addOnSuccessListener { onUploadComplete() }
                            .addOnFailureListener { error ->
                                onUploadFailed("Failed to save product: ${error.message}")
                            }
                    }
                }.addOnFailureListener { error ->
                    onUploadFailed("Failed to retrieve download URL for image $index: ${error.message}")
                }
            }
    }
}
