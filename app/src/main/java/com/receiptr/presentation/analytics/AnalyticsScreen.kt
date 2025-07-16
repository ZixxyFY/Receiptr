package com.receiptr.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.receiptr.domain.model.UiState
import com.receiptr.presentation.viewmodel.AnalyticsViewModel
import com.receiptr.ui.components.SkeletonAnalyticsContent
import com.receiptr.ui.theme.*

// Data models for analytics
data class MonthlyData(
    val month: String,
    val amount: Double,
    val percentage: Float // 0.0 to 1.0 for chart height
)

data class CategoryData(
    val category: String,
    val amount: Double,
    val percentage: Float // 0.0 to 1.0 for bar width
)

// TimePeriod enum is now in AnalyticsViewModel.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val analyticsState by viewModel.analyticsState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    
    // Sample data
    val monthlyData = remember {
        listOf(
            MonthlyData("Jan", 800.0, 0.4f),
            MonthlyData("Feb", 650.0, 0.4f),
            MonthlyData("Mar", 950.0, 0.6f),
            MonthlyData("Apr", 750.0, 0.5f),
            MonthlyData("May", 1200.0, 0.8f),
            MonthlyData("Jun", 1100.0, 0.8f),
            MonthlyData("Jul", 1234.0, 1.0f)
        )
    }
    
    val categoryData = remember {
        listOf(
            CategoryData("Food", 450.0, 1.0f),
            CategoryData("Travel", 45.0, 0.1f),
            CategoryData("Shopping", 315.0, 0.7f),
            CategoryData("Entertainment", 280.0, 0.7f),
            CategoryData("Other", 144.0, 0.6f)
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
            AnalyticsTopAppBar(navController)
            
            // Content with loading state
            when (val currentState = analyticsState) {
                is UiState.Loading, is UiState.Idle -> {
                    SkeletonAnalyticsContent()
                }
                
                is UiState.Success -> {
                    val analyticsData = currentState.data
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp), // Space for bottom nav
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            // Time Period Selector
                            TimePeriodSelector(
                                selectedPeriod = selectedPeriod,
                                onPeriodSelected = { period ->
                                    viewModel.selectTimePeriod(period)
                                }
                            )
                        }
                        
                        item {
                            // Spending Overview Chart
                            SpendingOverviewChart(
                                totalAmount = analyticsData.spendingAnalytics.totalSpending,
                                changeText = analyticsData.spendingAnalytics.spendingChange,
                                monthlyData = analyticsData.monthlyTrends
                            )
                        }
                        
                        item {
                            // Category Analysis
                            CategoryAnalysisChart(
                                totalAmount = analyticsData.spendingAnalytics.totalSpending,
                                changeText = analyticsData.spendingAnalytics.spendingChange,
                                categoryData = analyticsData.categoryBreakdown
                            )
                        }
                    }
                }
                
                is UiState.Error -> {
                    ErrorAnalyticsContent(
                        message = currentState.message,
                        onRetry = { viewModel.refreshAnalytics() }
                    )
                }
                
                is UiState.Empty -> {
                    EmptyAnalyticsContent(
                        onRefresh = { viewModel.refreshAnalytics() }
                    )
                }
            }
        }
        
        // Bottom Navigation
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            AnalyticsBottomNavigation(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsTopAppBar(navController: NavController) {
    TopAppBar(
        title = {
            Text(
                text = "Insights",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
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

@Composable
fun TimePeriodSelector(
    selectedPeriod: com.receiptr.presentation.viewmodel.TimePeriod,
    onPeriodSelected: (com.receiptr.presentation.viewmodel.TimePeriod) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            com.receiptr.presentation.viewmodel.TimePeriod.values().forEach { period ->
                val isSelected = period == selectedPeriod
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .selectable(
                            selected = isSelected,
                            onClick = { onPeriodSelected(period) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SpendingOverviewChart(
    totalAmount: Double,
    changeText: String,
    monthlyData: List<com.receiptr.data.analytics.MonthlySpending>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = "Spending",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$${String.format("%.2f", totalAmount)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                Text(
                    text = changeText,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bar Chart
            MonthlyBarChart(monthlyData)
        }
    }
}

@Composable
fun MonthlyBarChart(monthlyData: List<com.receiptr.data.analytics.MonthlySpending>) {
    if (monthlyData.isEmpty()) {
        // Show empty state
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }
    
    val maxAmount = monthlyData.maxOfOrNull { it.amount } ?: 1.0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        monthlyData.takeLast(7).forEach { monthData ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bar
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val percentage = if (maxAmount > 0) (monthData.amount / maxAmount).toFloat() else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(percentage)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Month Label
                Text(
                    text = monthData.month.takeLast(3), // Show last 3 chars (like "Jan")
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun BarChart(data: List<MonthlyData>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEach { monthData ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bar
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(monthData.percentage)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Month Label
                Text(
                    text = monthData.month,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun CategoryAnalysisChart(
    totalAmount: Double,
    changeText: String,
    categoryData: List<com.receiptr.data.analytics.CategorySpending>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Text(
            text = "Spending by Category",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = "Spending",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$${String.format("%.2f", totalAmount)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    Text(
                        text = changeText,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Horizontal Bar Chart
                CategoryBarChart(categoryData)
            }
        }
    }
}

@Composable
fun CategoryBarChart(categoryData: List<com.receiptr.data.analytics.CategorySpending>) {
    if (categoryData.isEmpty()) {
        // Show empty state
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No category data available",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }
    
    val maxAmount = categoryData.maxOfOrNull { it.amount } ?: 1.0
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categoryData.take(5).forEach { categorySpending ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Label
                Text(
                    text = categorySpending.category.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.width(80.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                ) {
                    val percentage = if (maxAmount > 0) (categorySpending.amount / maxAmount).toFloat() else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Amount
                Text(
                    text = "$${String.format("%.0f", categorySpending.amount)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun HorizontalBarChart(data: List<CategoryData>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        data.forEach { categoryData ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Label
                Text(
                    text = categoryData.category,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.width(80.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(categoryData.percentage)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsBottomNavigation(navController: NavController) {
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
            AnalyticsNavigationItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                isActive = false,
                onClick = { navController.navigate("home") }
            )
            
            // Scan
            AnalyticsNavigationItem(
                icon = Icons.Outlined.CameraAlt,
                label = "Scan",
                isActive = false,
                onClick = { navController.navigate("scan") }
            )
            
            // Insights (Active)
            AnalyticsNavigationItem(
                icon = Icons.Filled.Analytics,
                label = "Insights",
                isActive = true,
                onClick = { /* Already on analytics */ }
            )
            
            // Profile
            AnalyticsNavigationItem(
                icon = Icons.Outlined.Person,
                label = "Profile",
                isActive = false,
                onClick = { /* TODO: Navigate to profile */ }
            )
        }
    }
}

@Composable
fun AnalyticsNavigationItem(
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
            fontSize = 12.sp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorAnalyticsContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Something went wrong",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Try Again")
        }
    }
}

@Composable
fun EmptyAnalyticsContent(
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Analytics,
            contentDescription = "No data",
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No analytics data",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start by adding some receipts to see your spending insights",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Refresh")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    ReceiptrTheme {
        AnalyticsScreen(navController = rememberNavController())
    }
}
