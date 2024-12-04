package com.smartherd.aniaux.screens
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Column
import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smartherd.aniaux.R
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.smartherd.aniaux.AuthViewModel.AuthViewModel
import com.smartherd.aniaux.ui.theme.PurpleGrey40
import com.google.accompanist.pager.HorizontalPager
import com.smartherd.aniaux.dataViewModel.DataViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import coil.compose.AsyncImagePainter
import com.google.accompanist.pager.*
import com.smartherd.aniaux.components.shimmerBrush
import com.smartherd.aniaux.dataViewModel.getAnimalsByCategory
import com.smartherd.aniaux.models.Bid
import com.smartherd.aniaux.models.Product
import com.smartherd.aniaux.utils.Constants
import kotlinx.coroutines.launch

@Composable
fun Home(authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val cowBtn = remember { mutableStateOf(false) }
    val displayAll = remember { mutableStateOf(true) }
    val sheepBtn = remember { mutableStateOf(false) }
    val goatBtn = remember { mutableStateOf(false) }

    Spacer(modifier = Modifier.height(40.dp))
    Column(modifier = Modifier) {
        TopUpBAr(authViewModel)
        Spacer(modifier = Modifier.height(5.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Column(modifier = Modifier.clickable {
                    // Show all categories
                    displayAll.value = true
                    cowBtn.value = false
                    sheepBtn.value = false
                    goatBtn.value = false
                }) {
                    AlignYourBodyElement(
                        drawable = R.drawable.cattles,
                        text = R.string.all
                    )
                }
                Column(modifier = Modifier.clickable {
                    // Show only sheep category
                    displayAll.value = false
                    cowBtn.value = false
                    sheepBtn.value = true
                    goatBtn.value = false
                }) {
                    AlignYourBodyElement(
                        drawable = R.drawable.megansheep, // Image for sheep
                        text = R.string.sheep
                    )
                }

                Column(modifier = Modifier.clickable {
                    // Show only cow category
                    displayAll.value = false
                    cowBtn.value = true
                    sheepBtn.value = false
                    goatBtn.value = false
                }) {
                    AlignYourBodyElement(
                        drawable = R.drawable.luke_cow, // Image for cows
                        text = R.string.cow
                    )
                }

                Column(modifier = Modifier.clickable {
                    // Show only goat category
                    displayAll.value = false
                    cowBtn.value = false
                    sheepBtn.value = false
                    goatBtn.value = true
                }) {
                    AlignYourBodyElement(
                        drawable = R.drawable.florian_goat, // Image for goats
                        text = R.string.goat
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        // Display the selected category or all categories
        when {
            sheepBtn.value -> AnimalGrid("Sheep")
            cowBtn.value -> AnimalGrid("Cow")
            goatBtn.value -> AnimalGrid("Goat")
            displayAll.value -> AnimalDetailsGrid()
        }
    }
}
@Composable
fun AlignYourBodyElement(
    drawable: Int, // Keeping this as a resource ID for compatibility
    @StringRes text: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(end = 12.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(drawable)
                    .size(Size(80, 80)) // Resize the image to prevent large loads
                    .build()
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(75.dp)
                .clip(CircleShape)
        )
        Text(
            text = stringResource(text),
            modifier = Modifier.paddingFromBaseline(top = 24.dp, bottom = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpBAr(authViewModel: AuthViewModel){
    val context = LocalContext.current.applicationContext
    val coroutineScope = rememberCoroutineScope()
    TopAppBar(
        modifier = Modifier.height(90.dp),
        title = { Text(text = stringResource(R.string.app_name) , modifier = Modifier.padding(8.dp)) },
        navigationIcon = {
            IconButton(onClick = {

            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Image(
                        modifier = Modifier
                            .height(60.dp)
                            .size(65.dp),
                        painter = painterResource(id = R.drawable.aniuxlogo),
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                    )
                }



            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PurpleGrey40,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        actions = {
            IconButton(
                onClick = {//implementation feat to be added
                }
            ) {
                Icon(modifier = Modifier
                    .padding(bottom = 10.dp)
                    .align(Alignment.CenterVertically),
                    imageVector = Icons.Filled.Person, contentDescription = "Profile")
            }
            IconButton(onClick = {
                authViewModel.LogoutUser()
            }) {
                Icon( modifier = Modifier
                    .padding(bottom = 10.dp)
                    .align(Alignment.CenterVertically),
                    imageVector = Icons.Filled.ExitToApp, contentDescription = "Logout")
            }
        },

        )
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun AnimalDetailsGrid(dataViewModel: DataViewModel = viewModel()) {

    val showShimmer = remember { mutableStateOf(true) }
    val likeBtnState = remember { mutableStateOf(false) }
    val context = LocalContext.current.applicationContext
    val products = dataViewModel.state.value
    val openSheet = remember { mutableStateOf(false) }
    val selectedProduct = remember { mutableStateOf<Product?>(null) }
    val newBidPrice = remember { mutableStateOf("") }

    val sharedPreferences = context.getSharedPreferences(Constants.ANIAUX_PREFERENCES, Context.MODE_PRIVATE)
    val userName = sharedPreferences.getString(Constants.LOGGED_IN_USERNAME, "") ?: ""
    val userId = sharedPreferences.getString(Constants.LOGGED_IN_USER_ID, "") ?: ""

    if (openSheet.value) {
        selectedProduct.value?.let { product ->
            ModalBottomSheet(onDismissRequest = { openSheet.value = false },
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()

            ) {
                BottomSheetContent(
                    product = product,
                    newBidPrice = newBidPrice,
                    highestBidderPrice = product.bids.maxByOrNull { it.price }?.price ?: 0.0,
                    onPlaceBid = { bidAmount ->
                        val bid = Bid(userId, userName, product.id, bidAmount)
                        dataViewModel.placeBid(product.id, bid)
                        Toast.makeText(context, "Bid Placed Successfully", Toast.LENGTH_LONG).show()
                        openSheet.value = false
                        newBidPrice.value = ""
                    }
                )
            }
        }
    }
    Scaffold {
        if (products.isNotEmpty()) {
            LazyVerticalGrid(
                modifier = Modifier.padding(20.dp),
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(products) { product ->
                    ProductItem(
                        product = product,
                        onClick = {
                            selectedProduct.value = product
                            openSheet.value = true
                        },
                        isLiked = dataViewModel.likedStates.value[product.id] ?: false,
                        onLikeToggle = {
                            dataViewModel.toggleFavourite(product.id)
                            likeBtnState.value = !likeBtnState.value
                        }
                    )
                }
            }
        } else {
            EmptyState(showShimmer)
        }
    }
}
@OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun BottomSheetContent(
    product: Product,
    newBidPrice: MutableState<String>,
    highestBidderPrice: Double,
    onPlaceBid: (Double) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Box(modifier = Modifier
        .fillMaxHeight()
        .padding(16.dp)
        ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .navigationBarsPadding()

        ) {
            HorizontalPager(count = product.image_urls.size) { page ->
                val painter = rememberAsyncImagePainter(model = product.image_urls[page])
                Image(
                    painter = painter,
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .background(
                            shimmerBrush(
                                targetValue = 1300f,
                                showShimmer = painter.state is AsyncImagePainter.State.Loading
                            )
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = product.name, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Current Highest Bid: Ksh $highestBidderPrice", style = MaterialTheme.typography.bodyLarge)
            Column(modifier = Modifier.clickable {
                isExpanded = !isExpanded
            }) {
                Text(text = product.description,
                    maxLines = if(isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyLarge)
            }
            val bringIntoViewRequester = remember { BringIntoViewRequester() }
            val coroutineScope = rememberCoroutineScope()
            OutlinedTextField(
                value = newBidPrice.value,
                onValueChange = { newBidPrice.value = it },
                label = { Text("Enter your bid") },
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .fillMaxWidth()
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusEvent { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                        }
                    }
                ,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                newBidPrice.value.toDoubleOrNull()?.let { bidAmount ->
                    if (bidAmount >= highestBidderPrice) {
                        onPlaceBid(bidAmount)
                    } else {
                        Toast.makeText(context, "Your bid must be higher than the current highest bid", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text(text = "Place Bid")
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit,
    isLiked: Boolean,
    onLikeToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
       // product.image_urls.firstOrNull()?.let { imageUrl ->
            Image(
                painter = rememberAsyncImagePainter(model = product.image_urls[0]),
                contentDescription = "Grid image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
       // }
        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 5.dp)
        )
        Text(text = "Ksh ${product.price}", style = MaterialTheme.typography.bodyMedium)
        LikeButton(isLiked = isLiked, onClick = onLikeToggle)
    }
}
@Composable
fun LikeButton(isLiked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = "Like Button",
            modifier = Modifier
                .size(30.dp)
                .clickable { onClick() },
            tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.inverseSurface
        )
        Text(
            text = "Like",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Blue,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}
@Composable
fun EmptyState(showShimmer: MutableState<Boolean>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(shimmerBrush(showShimmer = true))
            .clip(RoundedCornerShape(8.dp))
            .padding(16.dp)
    )
}
@Composable
fun AnimalItem(
    product: Product,
    openSheet: MutableState<Boolean>,
    selectedProduct: MutableState<Product?>,
    dataViewModel: DataViewModel = viewModel()
) {
    val newBidPrice = remember { mutableStateOf("") }
    val context = LocalContext.current.applicationContext
    val sharedPreferences = context.getSharedPreferences(Constants.ANIAUX_PREFERENCES, Context.MODE_PRIVATE)
    val userName = sharedPreferences.getString(Constants.LOGGED_IN_USERNAME, "") ?: ""
    val userId = sharedPreferences.getString(Constants.LOGGED_IN_USER_ID, "") ?: ""

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    selectedProduct.value = product
                    openSheet.value = true
                    //Toast.makeText(context, "Selected product ID: ${product.id}", Toast.LENGTH_LONG).show()
                }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            product.image_urls.firstOrNull()?.let { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = "Grid image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = product.name, color = Color.Black)
            Text(text = product.price, color = Color.Black)
        }
    }
    if (openSheet.value) {
        selectedProduct.value?.let { animalProduct ->
            ProductDetailsBottomSheet(
                product = animalProduct,
                newBidPrice = newBidPrice,
                highestBidderPrice = animalProduct.bids.maxByOrNull { it.price }?.price ?: 0.0,
                onPlaceBid = { bidAmount ->
                    Toast.makeText(context, "Id for product is ${animalProduct.id} product name is ${animalProduct.name} ", Toast.LENGTH_LONG).show()
                    val bid = Bid(userId, userName, animalProduct.id, bidAmount)
                    dataViewModel.placeBid(product.id, bid)
                    Toast.makeText(context, "Bid Placed Successfully", Toast.LENGTH_LONG).show()
                    openSheet.value = false
                    newBidPrice.value = ""
                },
                onDismiss = { openSheet.value = false }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun ProductDetailsBottomSheet(
    product: Product,
    newBidPrice: MutableState<String>,
    highestBidderPrice: Double,
    onPlaceBid: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            HorizontalPager(count = product.image_urls.size) { page ->
                val painter = rememberAsyncImagePainter(model = product.image_urls[page])
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = product.name, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Current Highest Bid: Ksh $highestBidderPrice", style = MaterialTheme.typography.bodyLarge)
            Text(text = product.description, style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = newBidPrice.value,
                onValueChange = { newBidPrice.value = it },
                label = { Text("Enter your bid") },
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                newBidPrice.value.toDoubleOrNull()?.let { bidAmount ->
                    if (bidAmount > highestBidderPrice) {

                        onPlaceBid(bidAmount)
                    } else {
                        Toast.makeText(context, "Your bid must be higher than the current highest bid", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text(text = "Place Bid")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
@Composable
fun AnimalGrid(category: String) {
    var animals by remember { mutableStateOf<List<Product>>(emptyList()) }
    val openSheet = remember { mutableStateOf(false) }
    val selectedProduct = remember { mutableStateOf<Product?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<Exception?>(null) }
    LaunchedEffect(category) {
        getAnimalsByCategory(
            category,
            onSuccess = { products ->
                animals = products
                loading = false
                Log.d("AnimalProducts,", "${animals}")
            },
            onFailure = { e ->
                error = e
                loading = false
            }
        )
    }
    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Text("Error: ${error?.message}")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(animals) { animal ->
                AnimalItem(
                    product = animal,
                    openSheet = openSheet,
                    selectedProduct = selectedProduct,
                    dataViewModel = viewModel()
                )
            }
        }
    }
}
