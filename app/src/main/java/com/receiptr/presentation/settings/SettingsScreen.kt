package com.receiptr.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.receiptr.presentation.viewmodel.AuthViewModel
import com.receiptr.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Section
            item {
                UserProfileCard(currentUser = currentUser)
            }
            
            // Account Settings Section
            item {
                SettingsSection(
                    title = "Account",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Filled.Person,
                            title = "Profile",
                            description = "Edit your profile information",
                            onClick = { navController.navigate("profile") }
                        ),
                        SettingsItem(
                            icon = Icons.Filled.Email,
                            title = "Email Integration",
                            description = "Import receipts from your email",
                            onClick = { navController.navigate("email_integration") }
                        ),
                        SettingsItem(
                            icon = Icons.Filled.Security,
                            title = "Privacy & Security",
                            description = "Manage your account security",
                            onClick = { /* TODO: Implement privacy settings */ }
                        ),
                        SettingsItem(
                            icon = Icons.Filled.Notifications,
                            title = "Notifications",
                            description = "Configure notification preferences",
                            onClick = { navController.navigate("notifications") }
                        )
                    )
                )
            }
            
            // App Settings Section
            item {
                SettingsSection(
                    title = "App Settings",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.Outlined.Palette,
                            title = "Theme",
                            description = when (themeMode) {
                                SettingsViewModel.THEME_SYSTEM -> "Follow system theme"
                                SettingsViewModel.THEME_LIGHT -> "Light theme"
                                SettingsViewModel.THEME_DARK -> "Dark theme"
                                else -> "Change app appearance"
                            },
                            onClick = { navController.navigate("theme_settings") }
                        ),
                        SettingsItem(
                            icon = Icons.Outlined.Language,
                            title = "Language",
                            description = "Select your preferred language",
                            onClick = { /* TODO: Implement language settings */ }
                        ),
                        SettingsItem(
                            icon = Icons.Outlined.Storage,
                            title = "Storage",
                            description = "Manage app data and cache",
                            onClick = { /* TODO: Implement storage settings */ }
                        )
                    )
                )
            }
            
            // Support Section
            item {
                SettingsSection(
                    title = "Support",
                    items = listOf(
                        SettingsItem(
                            icon = Icons.AutoMirrored.Outlined.Help,
                            title = "Help & Support",
                            description = "Get help with using Receiptr",
                            onClick = { navController.navigate("help_center") }
                        ),
                        SettingsItem(
                            icon = Icons.Outlined.Feedback,
                            title = "Send Feedback",
                            description = "Share your thoughts with us",
                            onClick = { navController.navigate("contact_us") }
                        ),
                        SettingsItem(
                            icon = Icons.Outlined.Info,
                            title = "About",
                            description = "App version and information",
                            onClick = { /* TODO: Implement about */ }
                        )
                    )
                )
            }
            
            // Sign Out Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { authViewModel.signOut() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "Sign Out",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileCard(currentUser: com.receiptr.domain.model.User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Profile Picture Placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser?.displayName?.firstOrNull()?.toString()?.uppercase() ?: "U",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = currentUser?.displayName ?: "User",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = currentUser?.email ?: "user@example.com",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    items: List<SettingsItem>
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingsItemRow(
                        item = item,
                        showDivider = index < items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(
    item: SettingsItem,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { item.onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (item.description != null) {
                    Text(
                        text = item.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Go to ${item.title}",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
        
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
    }
}

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val description: String? = null,
    val onClick: () -> Unit
)
