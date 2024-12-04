package com.smartherd.aniaux.models

import android.net.Uri

data class ProductImage(
    var description: String = "",
    var name: String ="",
    val price: String ="",
    var uri: Uri? = null,
    val sellerId: String=""
)

data class Product(
    var id: String = "",
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val description: String = "",
    val image_urls: List<String> = emptyList(),
    val bids: List<Bid> = emptyList()
)
data class PurchasedProducts(
    var userId: String = "",
    var id: String = "",
    val name: String = "",
    val price: String = "",
    val image_url:String =  "",
)
