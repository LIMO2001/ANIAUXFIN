package com.smartherd.aniaux.screens
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint.Align
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Scaffold
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.smartherd.aniaux.AuthViewModel.AuthViewModel
import com.smartherd.aniaux.R
import com.smartherd.aniaux.components.shimmerBrush
import com.smartherd.aniaux.dataViewModel.DataViewModel
import com.smartherd.aniaux.models.Bid


import com.smartherd.aniaux.models.Product
import com.smartherd.aniaux.models.PurchasedProducts
import com.smartherd.aniaux.ui.theme.grey
import com.smartherd.aniaux.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun BidList(authViewModel: AuthViewModel, dataViewModel: DataViewModel, navController: NavController) {
    val context = LocalContext.current
    var showPaymentPopup by remember { mutableStateOf(false) }
    var selectedProductPrice by remember { mutableStateOf(0.0) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val openSheet = remember { mutableStateOf(false) }
    var mpesaNumber by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf("") }
    var selectedProductImage by remember { mutableStateOf("") }
    var selectedProductName by remember { mutableStateOf("") }
    var selectedPaymentOption by remember { mutableStateOf("M-Pesa") }
    var products = dataViewModel.state.value
    val newBidPrice = remember { mutableStateOf("") }
    val selectedProduct = remember { mutableStateOf<Product?>(null) }
    val showProgressBar = remember { mutableStateOf(false) }

    if (showProgressBar.value) {
        showProgressBar()
    }
    val sharedPreferences = context.getSharedPreferences(Constants.ANIAUX_PREFERENCES, Context.MODE_PRIVATE)
    val userName = sharedPreferences.getString(Constants.LOGGED_IN_USERNAME, "") ?: ""
    val userId = sharedPreferences.getString(Constants.LOGGED_IN_USER_ID, "") ?: ""

    fun calculateRemainingTime(bidTimestamp: Long): Long {
        val currentTime = System.currentTimeMillis()
        val timeRemaining = (10 * 60 * 1000L) - (currentTime - bidTimestamp)
        return timeRemaining.coerceAtLeast(0) // Ensure non-negative remaining time
    }

    fun formatTime(timeInMillis: Long): String {
        val minutes = (timeInMillis / (1000 * 60) % 60).toInt()
        val seconds = (timeInMillis / 1000 % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    @Composable
    fun AutoUpdatingCountdown(bidTimestamp: Long): String {
        var timeRemaining by remember { mutableStateOf(calculateRemainingTime(bidTimestamp)) }

        LaunchedEffect(timeRemaining) {
            if (timeRemaining > 0) {
                delay(1000L)
                timeRemaining = calculateRemainingTime(bidTimestamp)
            }
        }
        return formatTime(timeRemaining)
    }

    Scaffold {
        if (products.isNotEmpty()) {
            LazyColumn {
                items(products) { product ->
                    if (product.bids.isNotEmpty()) {
                        val highestBid = product.bids.maxByOrNull { it.price }
                        val highestBidder = highestBid?.userId ?: ""
                        val highestBidderName = highestBid?.username ?: ""
                        val highestBidderPrice = highestBid?.price ?: 0.0
                        highestBid?.let {
                            val countDown = calculateRemainingTime(it.timestamp)
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(model = product.image_urls[0]),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 20.sp,
                                    color = Color.Blue,
                                    modifier = Modifier.padding(8.dp)
                                )
                                highestBid?.let {
                                    Text(
                                        text = "Highest Bid: Ksh ${it.price}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 18.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                val currentTimeStamp = AutoUpdatingCountdown(highestBid.timestamp)
                                Text(
                                    text = "Time Remaining: ${currentTimeStamp}",
                                    fontSize = 18.sp,
                                    color = Color.Red,
                                    modifier = Modifier.padding(8.dp)
                                )
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (userId == highestBidder) {
                                        Button(
                                            onClick = {
                                                selectedProductId = product.id
                                                selectedProductImage = product.image_urls[0]
                                                selectedProductName = product.name
                                                selectedProductPrice = highestBid.price ?: 0.0
                                                showPaymentPopup = true
                                            },
                                            enabled = countDown == 0L, // Disable until timer expires
                                            modifier = Modifier.padding(3.dp)
                                        ) {
                                            Text(if (countDown == 0L) "Buy Now" else "Please Wait")
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                selectedProduct.value = product
                                                openSheet.value = true
                                            },
                                            modifier = Modifier.padding(3.dp)
                                        ) {
                                            Text("Bid Higher Now")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }





        else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(shimmerBrush(showShimmer = true))
                        .clip(RoundedCornerShape(8.dp))
                        .padding(5.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(shimmerBrush(showShimmer = true))
                        .clip(RoundedCornerShape(8.dp))
                        .padding(5.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(shimmerBrush(showShimmer = true))
                        .clip(RoundedCornerShape(8.dp))
                        .padding(5.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(shimmerBrush(showShimmer = true))
                        .clip(RoundedCornerShape(8.dp))
                        .padding(5.dp)
                )
            }
        }

        // Payment Popup
        var isPurchasing by remember { mutableStateOf(false) }

        if (showPaymentPopup) {
            AlertDialog(
                onDismissRequest = { showPaymentPopup = false },
                title = { Text("Payment Confirmation", color = Color.Black) },
                text = {
                    Column {
                        Text(
                            text = "Price: Ksh $selectedProductPrice",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Payment option selection
                        Text(
                            text = "Select Payment Method:",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedPaymentOption == "M-Pesa",
                                onClick = { selectedPaymentOption = "M-Pesa" }
                            )
                            Text("M-Pesa", modifier = Modifier.padding(start = 8.dp), color = Color.Green)
                        }
                        // M-Pesa Number Field
                        if (selectedPaymentOption == "M-Pesa") {
                            OutlinedTextField(
                                value = mpesaNumber,
                                onValueChange = { mpesaNumber = it },
                                label = { Text("Enter M-Pesa Number") },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    if (isPurchasing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Button(
                            onClick = {
                                isPurchasing = true
                                if (selectedPaymentOption == "M-Pesa" && mpesaNumber.isBlank()) {
                                    Toast.makeText(context, "Please enter your M-Pesa number", Toast.LENGTH_SHORT).show()
                                    isPurchasing = false
                                } else {
                                    val sharedPreferences = context.getSharedPreferences(
                                        Constants.ANIAUX_PREFERENCES,
                                        Context.MODE_PRIVATE
                                    )
                                    val userId = sharedPreferences.getString(Constants.LOGGED_IN_USER_ID, "") ?: ""
                                    if (userId.isNotEmpty() && selectedProductId.isNotBlank()) {
                                        val db = FirebaseFirestore.getInstance()

                                        db.collection("users").document(userId).get()
                                            .addOnSuccessListener { document ->
                                                if (document.exists()) {
                                                    db.collection("users")
                                                        .document(userId)
                                                        .update("purchased_products", FieldValue.arrayUnion(selectedProductId))
                                                        .addOnSuccessListener {
                                                            db.collection("purchased_products").add(
                                                                PurchasedProducts(
                                                                    userId = userId,
                                                                    id = selectedProductId,
                                                                    image_url = selectedProductImage,
                                                                    name = selectedProductName,
                                                                    price = selectedProductPrice.toString()
                                                                )
                                                            ).addOnSuccessListener {
                                                                db.collection("products").document(selectedProductId)
                                                                    .delete()
                                                                    .addOnSuccessListener {
                                                                        isPurchasing = false
                                                                        showPaymentPopup = false
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Purchase successful!",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                        products = products.filter { it.id !=selectedProductId }
                                                                    }
                                                                    .addOnFailureListener {
                                                                        isPurchasing = false
                                                                        Log.e("error", "Error deleting product: $it")
                                                                    }
                                                            }.addOnFailureListener {
                                                                isPurchasing = false
                                                                Log.e("error", "Error adding purchased product: $it")
                                                            }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isPurchasing = false
                                                            Toast.makeText(
                                                                context,
                                                                "Failed to record purchase: ${e.message}",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                } else {
                                                    isPurchasing = false
                                                    Toast.makeText(context, "User document not found. Please re-login.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                isPurchasing = false
                                                Toast.makeText(context, "Error retrieving user document: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        isPurchasing = false
                                        Toast.makeText(
                                            context,
                                            if (userId.isEmpty()) "User ID is missing. Please log in again." else "No product selected. Please try again.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            enabled = !isPurchasing, // Disable button if a purchase is in progress
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (isPurchasing) "Purchasing..." else "Confirm Payment",
                                color = Color.White
                            )
                        }
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showPaymentPopup = false },
                        enabled = !isPurchasing // Disable "Cancel" if a purchase is in progress
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        //paymentpopup end
    }


}
@Composable
fun showProgressBar(){
    CircularProgressIndicator(
        modifier = Modifier.width(70.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant

    )

}


