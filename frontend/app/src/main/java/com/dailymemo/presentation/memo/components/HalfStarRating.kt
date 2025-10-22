package com.dailymemo.presentation.memo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun HalfStarRating(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 32.dp,
    editable: Boolean = true,
    showLabel: Boolean = true,
    starColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..maxRating) {
                val starRating = when {
                    rating >= i -> 1f  // Full star
                    rating >= i - 0.5f -> 0.5f  // Half star
                    else -> 0f  // Empty star
                }

                Box(
                    modifier = Modifier
                        .size(starSize)
                        .clickable(enabled = editable) {
                            // Determine new rating based on click position
                            val newRating = when {
                                rating == i.toFloat() -> i - 0.5f  // If clicking on full star, make it half
                                rating == i - 0.5f -> i.toFloat()  // If clicking on half star, make it full
                                else -> i.toFloat()  // Otherwise make it full
                            }
                            onRatingChange(newRating.coerceIn(0f, maxRating.toFloat()))
                        }
                ) {
                    when (starRating) {
                        1f -> Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "$i stars",
                            tint = starColor,
                            modifier = Modifier.fillMaxSize()
                        )
                        0.5f -> Icon(
                            imageVector = Icons.Filled.StarHalf,
                            contentDescription = "$i - 0.5 stars",
                            tint = starColor,
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> Icon(
                            imageVector = Icons.Outlined.StarOutline,
                            contentDescription = "0 stars",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (i < maxRating) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }

        if (showLabel && rating > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "선택: ${rating}점",
                style = MaterialTheme.typography.bodySmall,
                color = starColor
            )
        }
    }
}

@Composable
fun HalfStarRatingDisplay(
    rating: Float,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 24.dp,
    starColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxRating) {
            val starRating = when {
                rating >= i -> 1f  // Full star
                rating >= i - 0.5f -> 0.5f  // Half star
                else -> 0f  // Empty star
            }

            when (starRating) {
                1f -> Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = starColor,
                    modifier = Modifier.size(starSize)
                )
                0.5f -> Icon(
                    imageVector = Icons.Filled.StarHalf,
                    contentDescription = null,
                    tint = starColor,
                    modifier = Modifier.size(starSize)
                )
                else -> Icon(
                    imageVector = Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}
