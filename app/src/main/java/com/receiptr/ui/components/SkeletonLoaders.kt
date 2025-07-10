package com.receiptr.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.receiptr.ui.theme.ReceiptrTheme

/**
 * Creates a shimmer effect for skeleton loading
 */
@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            ), label = "shimmer"
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

/**
 * Basic skeleton box component
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 16.dp,
    width: androidx.compose.ui.unit.Dp? = null,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .let { if (width != null) it.width(width) else it.fillMaxWidth() }
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush())
    )
}

/**
 * Skeleton circle for profile pictures or avatars
 */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 60.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(shimmerBrush())
    )
}

/**
 * Skeleton for receipt card items
 */
@Composable
fun SkeletonReceiptCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            // Category icon skeleton
            SkeletonCircle(size = 48.dp)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Store name skeleton
                SkeletonBox(height = 16.dp, width = 120.dp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date skeleton
                SkeletonBox(height = 12.dp, width = 80.dp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(horizontalAlignment = Alignment.End) {
                // Amount skeleton
                SkeletonBox(height = 18.dp, width = 80.dp)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Category skeleton
                SkeletonBox(height = 12.dp, width = 60.dp)
            }
        }
    }
}

/**
 * Skeleton for welcome card
 */
@Composable
fun SkeletonWelcomeCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar skeleton
            SkeletonCircle(size = 60.dp)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Greeting skeleton
                SkeletonBox(height = 14.dp, width = 100.dp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Name skeleton
                SkeletonBox(height = 20.dp, width = 150.dp)
            }
            
            // Notification icon skeleton
            SkeletonCircle(size = 32.dp)
        }
    }
}

/**
 * Skeleton for spending overview card
 */
@Composable
fun SkeletonSpendingCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "This Month" skeleton
                SkeletonBox(height = 16.dp, width = 80.dp)
                
                // Trending icon skeleton
                SkeletonCircle(size = 24.dp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Amount skeleton
            SkeletonBox(height = 32.dp, width = 140.dp)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Percentage change skeleton
            SkeletonBox(height = 14.dp, width = 120.dp)
        }
    }
}

/**
 * Skeleton for quick action cards
 */
@Composable
fun SkeletonQuickActionCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(100.dp)
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon skeleton
            SkeletonCircle(size = 32.dp)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title skeleton
            SkeletonBox(height = 12.dp, width = 60.dp)
        }
    }
}

/**
 * Skeleton for analytics chart
 */
@Composable
fun SkeletonAnalyticsChart(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Chart title skeleton
            SkeletonBox(height = 18.dp, width = 150.dp)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chart bars skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) {
                    SkeletonBox(
                        height = (40..120).random().dp,
                        width = 24.dp,
                        cornerRadius = 4.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Chart labels skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) {
                    SkeletonBox(height = 12.dp, width = 20.dp)
                }
            }
        }
    }
}

/**
 * Complete skeleton for home screen content
 */
@Composable
fun SkeletonHomeContent(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp), // Space for bottom nav
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Welcome card skeleton
            SkeletonWelcomeCard()
        }
        
        item {
            // Spending overview skeleton
            SkeletonSpendingCard()
        }
        
        item {
            // Quick actions section
            Column {
                SkeletonBox(height = 18.dp, width = 120.dp) // "Quick Actions" title
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(4) {
                        SkeletonQuickActionCard()
                    }
                }
            }
        }
        
        item {
            // Recent receipts section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SkeletonBox(height = 18.dp, width = 140.dp) // "Recent Receipts" title
                    SkeletonBox(height = 16.dp, width = 60.dp) // "View All" link
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        // Receipt cards skeleton
        items(3) {
            SkeletonReceiptCard()
        }
    }
}

/**
 * Skeleton for receipts screen content
 */
@Composable
fun SkeletonReceiptsContent(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(top = 8.dp)
    ) {
        item {
            // Section title skeleton
            SkeletonBox(
                height = 22.dp,
                width = 160.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            )
        }
        
        items(6) {
            SkeletonReceiptCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Skeleton for analytics screen content
 */
@Composable
fun SkeletonAnalyticsContent(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            // Time period selector skeleton
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(3) {
                        SkeletonBox(
                            height = 40.dp,
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                            cornerRadius = 8.dp
                        )
                    }
                }
            }
        }
        
        item {
            // Spending overview chart skeleton
            SkeletonAnalyticsChart(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        item {
            // Category analysis skeleton
            SkeletonAnalyticsChart(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SkeletonPreview() {
    ReceiptrTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonWelcomeCard()
            SkeletonReceiptCard()
            SkeletonQuickActionCard()
        }
    }
}
