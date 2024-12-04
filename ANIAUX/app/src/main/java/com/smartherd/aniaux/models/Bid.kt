package com.smartherd.aniaux.models

data class Bid(
    val userId: String = "",
    val username: String = "",
    val productId: String = "",
    val price: Double = 0.0, // Consider using a Double for bid amounts
    val timestamp: Long = System.currentTimeMillis(),
)

