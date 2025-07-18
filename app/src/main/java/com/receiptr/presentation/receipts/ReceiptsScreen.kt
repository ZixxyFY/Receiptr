package com.receiptr.presentation.receipts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.material.*
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.receiptr.domain.model.Receipt
import com.receiptr.presentation.viewmodel.ReceiptsViewModel
import com.receiptr.ui.components.SkeletonReceiptsContent
import com.receiptr.ui.components.HelpfulGuideEmptyState
import com.receiptr.ui.components.QuickTipEmptyState
import com.receiptr.ui.components.EncouragementEmptyState
import com.receiptr.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsScreen(
    navController: NavController
) {
    // ViewModel for managing receipts data
    val viewModel: ReceiptsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val receipts = uiState.receipts
    val isLoading = uiState.isLoading
    val error = uiState.error
    
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
                } else if (receipts.isNotEmpty()) {
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
                            SwipeableReceiptCard(
                                receipt = receipt,
                                onDelete = { deletedReceipt ->
                                    viewModel.deleteReceipt(deletedReceipt.id)
                                },
                                onClick = { 
                                    navController.navigate("receipt_detail/${receipt.id}")
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                } else {
                    // Display creative empty state
                    EncouragementEmptyState(onStartTrackingClick = { navController.navigate("scan") })
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
fun SwipeableReceiptCard(
    receipt: Receipt,
    onDelete: (Receipt) -> Unit,
    onClick: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(true) }
    var isDeleted by remember { mutableStateOf(false) }
    
    AnimatedVisibility(
        visible = isVisible && !isDeleted,
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background with delete action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Main card content
            ReceiptCard(
                receipt = receipt,
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

@Composable
fun ReceiptCard(
    receipt: Receipt,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
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
                    text = receipt.category.ifEmpty { "Other" },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Store Name
                Text(
                    text = receipt.merchantName.ifEmpty { "Unknown Store" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Amount and Date
                Text(
                    text = "$${String.format("%.2f", receipt.totalAmount)} · ${SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date(receipt.date))}",
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
