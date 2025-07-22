package com.receiptr.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.receiptr.presentation.viewmodel.AuthViewModel
import com.receiptr.presentation.viewmodel.SettingsViewModel

// Profile menu item data
data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector? = null,
    val value: String? = null,
    val hasToggle: Boolean = false,
    val toggleValue: Boolean = false,
    val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val darkModeEnabled by settingsViewModel.isDarkModeEnabled.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header with back button and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = "Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 48.dp),
                textAlign = TextAlign.Center
            )
        }
        
        // Scrollable content
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                // User Profile Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture - circular background with Person icon
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // User Name
                    Text(
                        text = currentUser?.displayName ?: currentUser?.email?.substringBefore('@') ?: "User",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // User Email
                    Text(
                        text = currentUser?.email ?: "user@email.com",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            item {
                // Preferences Section
                Text(
                    text = "Preferences",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
                
                ProfileMenuItemFigma(
                    title = "Dark Mode",
                    hasToggle = true,
                    toggleValue = darkModeEnabled,
                    onToggleChange = { settingsViewModel.toggleDarkMode() }
                )
            }
            
            item {
                // Account Section
                Text(
                    text = "Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
                
                ProfileMenuItemFigma(
                    title = "Change Password",
                    hasArrow = true,
                    onClick = { navController.navigate("change_password") }
                )
                
                ProfileMenuItemFigma(
                    title = "Notifications",
                    hasArrow = true,
                    onClick = { navController.navigate("notifications") }
                )
                
                ProfileMenuItemFigma(
                    title = "Language",
                    value = "English"
                )
            }
            
            item {
                // Support Section
                Text(
                    text = "Support",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
                
                ProfileMenuItemFigma(
                    title = "Help Center",
                    hasArrow = true,
                    onClick = { navController.navigate("help_center") }
                )
                
                ProfileMenuItemFigma(
                    title = "Contact Us",
                    hasArrow = true,
                    onClick = { navController.navigate("contact_us") }
                )
            }
        }
        
        // Bottom section with logout button
        Column {
            // Logout Button
            Button(
                onClick = {
                    viewModel.signOut()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Log Out",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Bottom spacing
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}

@Composable
fun ProfileMenuItemFigma(
    title: String,
    value: String? = null,
    hasToggle: Boolean = false,
    toggleValue: Boolean = false,
    hasArrow: Boolean = false,
    onClick: () -> Unit = {},
    onToggleChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable { if (!hasToggle) onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title
        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        
        // Right side content
        when {
            hasToggle -> {
                // Custom toggle switch that matches Figma design
                Box(
                    modifier = Modifier
                        .width(51.dp)
                        .height(31.dp)
                        .clip(RoundedCornerShape(15.5.dp))
                        .background(
                            if (toggleValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onToggleChange(!toggleValue) }
                        .padding(2.dp),
                    contentAlignment = if (toggleValue) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .size(27.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .shadow(3.dp, CircleShape)
                    )
                }
            }
            value != null -> {
                // Value text
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            hasArrow -> {
                // Arrow icon
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}
