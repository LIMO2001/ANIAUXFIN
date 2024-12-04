package com.smartherd.aniaux.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.smartherd.aniaux.AuthViewModel.AuthViewModel
import com.smartherd.aniaux.components.shimmerBrush
import com.smartherd.aniaux.dataViewModel.DataViewModel
import com.smartherd.aniaux.models.Product

@Composable
fun Favourite(dataViewModel: DataViewModel, authViewModel: AuthViewModel) {
    val favouriteItems by dataViewModel.favourites
    val isLoading by dataViewModel.loading

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    // Show shimmer effect while loading
                    Column(modifier = Modifier.fillMaxWidth()) {
                        repeat(3) {
                            Spacer(modifier = Modifier.height(5.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(shimmerBrush(showShimmer = true))
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                            )
                        }
                    }
                }
                favouriteItems.isEmpty() -> {
                    // Show "No Items in Favourites" message when no items are loaded
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Items in Favourites", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                else -> {
                    // Show list of favorite items if they are available
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        favouriteItems.forEach { product ->
                            ProductCarouselCard(product)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCarouselCard(product: Product) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            // Image carousel for each product
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(product.image_urls) { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = "Product Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .heightIn(180.dp)
                            .height(180.dp)
                            .widthIn(220.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            // Product name
            Text(
                text = product.name,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Product price
            Text(
                text = "Price: Ksh ${product.price}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            // Number of likes
            Text(
                text = "1000 Likes",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier.padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // Display the product image
            product.image_urls.firstOrNull()?.let { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = "Product Image",
                    modifier = Modifier.size(120.dp)
                )
            }

            // Product name
            Text(
                text = product.name,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Product price
            Text(
                text = "Price: Ksh ${product.price}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            // Number of likes
            Text(
                text = "200 Likes",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
