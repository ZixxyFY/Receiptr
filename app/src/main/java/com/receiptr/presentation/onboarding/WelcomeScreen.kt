package com.receiptr.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.receiptr.R
import com.receiptr.ui.theme.ReceiptrTheme

// Define the design system colors from your Figma
object WelcomeColors {
    val Background = Color(0xFFF8FCFA)
    val DarkGreen = Color(0xFF0C1C17)
    val PrimaryGreen = Color(0xFF019863)
    val SecondaryGreen = Color(0xFF46A080)
    val BorderGreen = Color(0xFFE6F4EF)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Main Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Top Header
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.receiptr_logo),
                        contentDescription = "Receiptr Logo",
                        modifier = Modifier
                            .height(32.dp)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                },
                actions = {
                    IconButton(
                        onClick = { /* Help action */ }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Help",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
            
            // Welcome Title
            Text(
                text = "Welcome to Receiptr",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp, bottom = 12.dp)
            )
            
            // Subtitle
            Text(
                text = "Scan, organize, and manage your receipts effortlessly.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
            
            // Hero Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(2f / 3f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Placeholder for the hero image
                        // You can replace this with your actual receipt scanning illustration
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Receipt,
                                contentDescription = "Receipt Scanning",
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Smart Receipt\nScanning",
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            // Get Started Button
            Button(
                onClick = {
                    navController.navigate("login")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Create Account Button
            OutlinedButton(
                onClick = {
                    navController.navigate("registration")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Bottom Navigation
        BottomNavigationBar()
    }
}

@Composable
fun BottomNavigationBar() {
    Column {
        // Divider
        HorizontalDivider(
            color = WelcomeColors.BorderGreen,
            thickness = 1.dp
        )
        
        // Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WelcomeColors.Background)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Home (Active)
            NavigationItem(
                icon = Icons.Filled.Home,
                isActive = true
            )
            
            // Receipts
            NavigationItem(
                icon = Icons.Outlined.Receipt,
                isActive = false
            )
            
            // Add/Scan
            NavigationItem(
                icon = Icons.Outlined.Add,
                isActive = false
            )
            
            // Analytics
            NavigationItem(
                icon = Icons.Outlined.Analytics,
                isActive = false
            )
            
            // Profile
            NavigationItem(
                icon = Icons.Outlined.Person,
                isActive = false
            )
        }
        
        // Bottom spacing
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(WelcomeColors.Background)
        )
    }
}

@Composable
fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean
) {
    IconButton(
        onClick = { /* Navigation logic */ },
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) WelcomeColors.DarkGreen else WelcomeColors.SecondaryGreen,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    ReceiptrTheme {
        WelcomeScreen(navController = rememberNavController())
    }
}
