package com.smartherd.aniaux.dataViewModel

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.smartherd.aniaux.models.Bid
import com.smartherd.aniaux.models.Product
import com.smartherd.aniaux.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.CoroutineContext

class DataViewModel: ViewModel(){
    // val category = mutableStateOf("")
    val state = mutableStateOf<List<Product>>(emptyList())
    val favourites = mutableStateOf<List<Product>>(emptyList())
    val loading = mutableStateOf(true)
    val likedStates = mutableStateOf<Map<String, Boolean>>(emptyMap())
    // Store timers for each product
    private val productTimers = mutableMapOf<String, Job>()
    private val timerScope = CoroutineScope(Dispatchers.IO)
    val countdown = mutableStateOf("")
    private var countdownTimer: CountDownTimer? = null
    init {
        getData()
        getFavourites()
      //  getDataByCategory(category.toString())
    }
    private fun getData(){
        viewModelScope.launch {
            state.value = getAnimalProductsFromFireStore()
        }
    }
    //timer
    fun startCountdown(productId: String) {
        viewModelScope.launch {
            val product = getProductById(productId)
            val highestBid = product?.bids?.maxByOrNull { it.price }
            highestBid?.let {
                val bidTime = it.timestamp
                val currentTime = System.currentTimeMillis()

                // Calculate time remaining in milliseconds (4 hours - time since bid)
                val timeRemaining = (4 * 60 * 60 * 1000L) - (currentTime - bidTime)

                // If there's time remaining, start the countdown
                if (timeRemaining > 0) {
                    countdownTimer?.cancel() // Cancel any previous timer
                    countdownTimer = object : CountDownTimer(timeRemaining, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            countdown.value = formatTime(millisUntilFinished)
                        }

                        override fun onFinish() {
                            countdown.value = "00:00:00"
                            // You could also trigger auto-buy logic here if needed
                        }
                    }.start()
                } else {
                    countdown.value = "00:00:00"
                }
            }
        }
    }

    // Helper function to format time as hh:mm:ss
    private fun formatTime(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millis % (1000 * 60)) / 1000
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    // New function to save the bid
    fun placeBid(productId: String, bid: Bid) {
        viewModelScope.launch {
            saveBidToFirestore(productId, bid)
            // Start or reset the 4-hour timer for this product
            productTimers[productId]?.cancel() // Cancel any existing timer for the product
            productTimers[productId] = timerScope.launch {
                delay(4 * 60 * 60 * 1000L) // 4 hours in milliseconds
                checkAndAutoBuyProduct(productId)
            }
        }
    }

    suspend fun checkAndAutoBuyProduct(productId: String) {
        val product = getProductById(productId)
        val highestBid = product?.bids?.maxByOrNull { it.price }

        // If there is a highest bid and no other bids were placed in the last 4 hours, auto-buy the product
        highestBid?.let {
            val bidTime = it.timestamp
            val currentTime = System.currentTimeMillis()

            // Check if 4 hours have passed since the highest bid was placed
            if ((currentTime - bidTime) >= (4 * 60 * 60 * 1000)) {
                autoBuyProduct(productId, highestBid)
            }
        }
    }
    private fun autoBuyProduct(productId: String, highestBid: Bid) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val userId = highestBid.userId // Assuming Bid has a `userId` field

            db.collection("users")
                .document(userId)
                .update("purchased_products", FieldValue.arrayUnion(productId))
                .addOnSuccessListener {
                    Log.d("Auto-buy", "Product $productId auto-bought by user $userId.")
                }
                .addOnFailureListener { e ->
                    Log.e("Auto-buy", "Failed to auto-buy product: ${e.message}", e)
                }
        }
    }
    // Toggle favourite status and update Firestore
    fun toggleFavourite(productId: String) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val isLiked = likedStates.value[productId] ?: false
        val updatedMap = likedStates.value.toMutableMap()
        updatedMap[productId] = !isLiked
        likedStates.value = updatedMap // Update the liked state locally

        val favouritesRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("favourites")

        if (!isLiked) {
            // Add product to favourites
            favouritesRef.document(productId).set(mapOf("productId" to productId))
        } else {
            // Remove product from favourites
            favouritesRef.document(productId).delete()
        }
    }

    private fun getFavourites() {
        loading.value = true
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val favouritesRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("favourites")

            val favouriteProducts = mutableListOf<Product>()
            val likedMap = mutableMapOf<String, Boolean>() // Temporary map to store liked state
            try {
                val documents = favouritesRef.get().await()
                for (doc in documents) {
                    val productId = doc.getString("productId") ?: continue
                    likedMap[productId] = true // Mark as liked
                    val product = getProductById(productId)
                    product?.let { favouriteProducts.add(it) }
                }
                loading.value = false
                favourites.value = favouriteProducts
                likedStates.value = likedMap // Initialize liked states
            } catch (e: FirebaseFirestoreException) {
                Log.e("Firestore", "Error fetching favourites", e)
                loading.value = false
            }
        }
    }
    // Helper function to retrieve product details by ID
    private suspend fun getProductById(productId: String): Product? {
        return try {
            val doc = FirebaseFirestore.getInstance()
                .collection("products")
                .document(productId)
                .get()
                .await()
            doc.toObject(Product::class.java)
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting product by ID", e)
            null
        }
    }

}
private suspend fun getAnimalProductsFromFireStore(): List<Product> {
    val db = FirebaseFirestore.getInstance()
    val products = mutableListOf<Product>()

    try {
        val results = db.collection("products").get().await()

        for (document in results) {
            // Use the document ID to create the Product instance
            val product = document.toObject(Product::class.java).copy(id = document.id)
            products.add(product)

        }
        Log.d("products of animals Firestore", "$products")
    } catch (e: FirebaseFirestoreException) {
        Log.d("Error Fetching data from Firestore", "$e")
    }

    return products
}

private suspend fun saveBidToFirestore(productId: String, bid: Bid) {
    val db = FirebaseFirestore.getInstance()
    // Add the bid to the bids array in the product document
    db.collection("products")
        .document(productId)
        .update("bids", FieldValue.arrayUnion(bid))
        .addOnSuccessListener {
            Log.d("Firestore", "Bid successfully added!")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error adding bid", e)
        }
}
fun saveUserPreferences(userName: String, email: String, uid: String, userRole: String, picUrl: String, context: Context) {
    val sharedPreferences = context.getSharedPreferences(Constants.ANIAUX_PREFERENCES, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(Constants.LOGGED_IN_USERNAME, userName)
    editor.putString(Constants.LOGGED_IN_USEREMAIL, email)
    editor.putString(Constants.LOGGED_IN_USER_ID, uid)
    editor.putString(Constants.ROLE, userRole)
    editor.putString(Constants.PROFILE_PICTURE, picUrl)
    editor.apply() // Commit the changes
}
fun getAnimalsByCategory(category: String,  onSuccess: (List<Product>) -> Unit, onFailure: (Exception) -> Unit ){
    val db = FirebaseFirestore.getInstance()

    db.collection("products").whereEqualTo("category" ,category)

        .get()
        .addOnSuccessListener { success ->
            val products = success.documents.mapNotNull { document ->
                val product = document.toObject(Product::class.java)
                product?.apply { id = document.id }
            }
            onSuccess(products)
            Log.d("Products from firebase", "${products }")

        }.addOnFailureListener{e ->
            onFailure(e)

        }
}

