package com.receiptr.presentation.receipts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.receiptr.ui.components.SkeletonReceiptsContent
import com.receiptr.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Receipt data model for the receipts screen
data class Receipt(
    val id: String,
    val category: String,
    val storeName: String,
    val amount: Double,
    val date: Date,
    val imageUrl: String? = null // For future implementation
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsScreen(
    navController: NavController
) {
    // Loading state to simulate data loading
    var isLoading by remember { mutableStateOf(true) }
    
    // Simulate loading delay
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000) // 2 second delay
        isLoading = false
    }
    
    // Sample receipt data
    val receipts = remember {
        listOf(
            Receipt(
                id = "1",
                category = "Groceries",
                storeName = "Fresh Foods Market",
                amount = 52.75,
                date = Calendar.getInstance().apply { 
                    set(2024, Calendar.JULY, 15) 
                }.time
            ),
            Receipt(
                id = "2",
                category = "Travel",
                storeName = "Airways Express",
                amount = 245.00,
                date = Calendar.getInstance().apply { 
                    set(2024, Calendar.JULY, 12) 
                }.time
            ),
            Receipt(
                id = "3",
                category = "Shopping",
                storeName = "Fashion Emporium",
                amount = 89.99,
                date = Calendar.getInstance().apply { 
                    set(2024, Calendar.JULY, 10) 
                }.time
            ),
            Receipt(
                id = "4",
                category = "Food & Dining",
                storeName = "Pizza Palace",
                amount = 28.50,
                date = Calendar.getInstance().apply { 
                    set(2024, Calendar.JULY, 8) 
                }.time
            ),
            Receipt(
                id = "5",
                category = "Transportation",
                storeName = "Metro Transit",
                amount = 15.00,
                date = Calendar.getInstance().apply { 
                    set(2024, Calendar.JULY, 5) 
                }.time
            )
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header
            ReceiptsTopAppBar(navController)
            
            // Scan Receipt Button
            ScanReceiptButton(navController)
            
            // Content with loading state
            if (isLoading) {
                SkeletonReceiptsContent()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp), // Space for bottom nav
                    contentPadding = PaddingValues(top = 8.dp)
                ) {
                    item {
                        // Section Title
                        Text(
                            text = "Recent Receipts",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                        )
                    }
                    
                    items(receipts) { receipt ->
                        ReceiptCard(receipt = receipt)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        
        // Bottom Navigation - positioned at bottom
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReceiptsBottomNavigation(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsTopAppBar(navController: NavController) {
    TopAppBar(
        title = {
            Text(
                text = "Receiptr",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        actions = {
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun ScanReceiptButton(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { navController.navigate("scan") },
            modifier = Modifier.height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "Camera",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Scan Receipt",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ReceiptCard(receipt: Receipt) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Receipt Info (takes up 2/3 of space)
            Column(
                modifier = Modifier.weight(2f)
            ) {
                // Category
                Text(
                    text = receipt.category,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Store Name
                Text(
                    text = receipt.storeName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Amount and Date
                Text(
                    text = "$${String.format("%.2f", receipt.amount)} · ${SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(receipt.date)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Receipt Image Placeholder (takes up 1/3 of space)
            ReceiptImagePlaceholder(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(16f / 9f) // Video aspect ratio as in Figma
            )
        }
    }
}

@Composable
fun ReceiptImagePlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Receipt,
            contentDescription = "Receipt Image",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun ReceiptsBottomNavigation(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Home
            ReceiptsNavigationItem(
                icon = Icons.Filled.Home,
                label = "Home",
                isActive = false,
                onClick = { navController.navigate("home") }
            )
            
            // Receipts/Recent (Active - showing clock icon as in Figma)
            ReceiptsNavigationItem(
                icon = Icons.Outlined.History,
                label = "Recent",
                isActive = true,
                onClick = { /* Already on receipts */ }
            )
            
            // Analytics/Chart
            ReceiptsNavigationItem(
                icon = Icons.Outlined.Analytics,
                label = "Analytics",
                isActive = false,
                onClick = { navController.navigate("analytics") }
            )
            
            // Profile
            ReceiptsNavigationItem(
                icon = Icons.Outlined.Person,
                label = "Profile",
                isActive = false,
                onClick = { /* TODO: Navigate to profile */ }
            )
        }
    }
}

@Composable
fun ReceiptsNavigationItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReceiptsScreenPreview() {
    ReceiptrTheme {
        ReceiptsScreen(navController = rememberNavController())
    }
}
