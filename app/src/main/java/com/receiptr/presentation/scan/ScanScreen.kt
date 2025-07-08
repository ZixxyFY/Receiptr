package com.receiptr.presentation.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.receiptr.ui.theme.*

@Composable
fun ScanScreen(
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ReceiptrBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header with Close Button
            ScanTopAppBar(navController)
            
            // Camera Viewfinder Area (placeholder)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.1f)), // Camera preview placeholder
                contentAlignment = Alignment.Center
            ) {
                // Camera preview would go here
                // For now, showing a placeholder
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Camera Preview",
                        modifier = Modifier.size(64.dp),
                        tint = ReceiptrSecondaryGreen.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Preview",
                        color = ReceiptrSecondaryGreen.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Point camera at receipt",
                        color = ReceiptrSecondaryGreen.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }
            
            // Camera Controls
            CameraControls()
            
            // Bottom Section
            ScanBottomSection(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTopAppBar(navController: NavController) {
    TopAppBar(
        title = { }, // Empty title
        actions = {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = ReceiptrDarkGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ReceiptrBackground
        )
    )
}

@Composable
fun CameraControls() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gallery Button
        CameraControlButton(
            icon = Icons.Outlined.Image,
            size = 40.dp,
            onClick = { /* TODO: Open gallery */ }
        )
        
        // Main Camera Button (larger)
        CameraControlButton(
            icon = Icons.Filled.CameraAlt,
            size = 64.dp,
            onClick = { /* TODO: Take photo */ }
        )
        
        // Effects/Filters Button (spiral icon placeholder)
        CameraControlButton(
            icon = Icons.Outlined.Tune, // Using tune icon as spiral placeholder
            size = 40.dp,
            onClick = { /* TODO: Open effects */ }
        )
    }
}

@Composable
fun CameraControlButton(
    icon: ImageVector,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.4f),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(if (size > 50.dp) 32.dp else 20.dp)
        )
    }
}

@Composable
fun ScanBottomSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ReceiptrBackground)
    ) {
        // Scan Receipt Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { 
                    // TODO: Process scanned receipt
                    // For now, navigate back to receipts
                    navController.navigate("receipts") {
                        popUpTo("scan") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .widthIn(min = 160.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ReceiptrPrimaryGreen,
                    contentColor = ReceiptrBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Receipt,
                    contentDescription = "Receipt",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Scan Receipt",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Bottom Sheet Handle (as in Figma)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ReceiptrBackground),
            contentAlignment = Alignment.TopCenter
        ) {
            // Handle bar
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ReceiptrBorderGreen)
            )
        }
        
        // Bottom spacing
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(ReceiptrBackground)
        )
    }
}

// Camera Permission and functionality would be implemented here
@Composable
fun CameraPreview() {
    // TODO: Implement actual camera preview using CameraX
    // This would require camera permissions and CameraX dependencies
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Camera Preview\n(Requires CameraX implementation)",
            color = Color.White,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    ReceiptrTheme {
        ScanScreen(navController = rememberNavController())
    }
}
