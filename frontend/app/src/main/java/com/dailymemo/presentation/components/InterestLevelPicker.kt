package com.dailymemo.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InterestLevelPicker(
    currentLevel: Float,
    onLevelChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLevelInt = currentLevel.toInt()
    Column(modifier = modifier) {
        Text(
            text = "관심도",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(5) { index ->
                val level = index + 1
                IconButton(
                    onClick = { onLevelChange(level.toFloat()) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (level <= currentLevelInt) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = "관심도 $level",
                        tint = if (level <= currentLevelInt) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Text(
            text = when (currentLevelInt) {
                0 -> "관심도를 선택하세요"
                1 -> "조금 관심있어요"
                2 -> "관심있어요"
                3 -> "매우 관심있어요"
                4 -> "꼭 가보고 싶어요"
                5 -> "반드시 가야해요!"
                else -> ""
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
