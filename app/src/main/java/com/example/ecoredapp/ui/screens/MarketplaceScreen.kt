package com.example.ecoredapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Estructura de datos del producto
data class Product(val id: String, val name: String, val price: String, val imageUrl: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen() {
    // Lista simulada de la base de datos con imágenes reales de internet
    val products = listOf(
        Product("1", "Termo Ecológico", "150 Puntos", "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=500&q=60"),
        Product("2", "Bolsa de Tela", "50 Puntos", "https://images.unsplash.com/photo-1597348989645-46b190ce4918?auto=format&fit=crop&w=500&q=60"),
        Product("3", "Cepillo de Bambú", "30 Puntos", "https://images.unsplash.com/photo-1605197148906-8167f5b72186?auto=format&fit=crop&w=500&q=60"),
        Product("4", "Libreta Reciclada", "80 Puntos", "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=500&q=60")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EcoTienda", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        // Aquí está el LazyVerticalGrid que pedía la tarea
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF1F3F4)).padding(8.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                ProductCard(product)
            }
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // AsyncImage de Coil descarga la imagen sin trabar la app
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = product.price, color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Canjear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}