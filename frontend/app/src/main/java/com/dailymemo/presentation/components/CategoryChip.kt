package com.dailymemo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dailymemo.domain.models.CategorySentiment
import com.dailymemo.domain.models.MemoCategory

/**
 * 개별 카테고리를 표시하는 칩 컴포넌트
 *
 * 감정(sentiment)에 따라 색상이 자동으로 변경됩니다:
 * - POSITIVE: 밝은 초록색 배경 / 진한 초록색 텍스트
 * - NEGATIVE: 밝은 빨간색 배경 / 진한 빨간색 텍스트
 * - NEUTRAL: 밝은 회색 배경 / 진한 회색 텍스트
 *
 * @param category 표시할 카테고리
 * @param modifier Modifier
 */
@Composable
fun CategoryChip(
    category: MemoCategory,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (category.sentiment) {
        CategorySentiment.POSITIVE -> Color(0xFFDCFCE7) // Light green
        CategorySentiment.NEGATIVE -> Color(0xFFFEE2E2) // Light red
        CategorySentiment.NEUTRAL -> Color(0xFFF3F4F6)  // Light gray
    }
    val textColor = when (category.sentiment) {
        CategorySentiment.POSITIVE -> Color(0xFF16A34A) // Dark green
        CategorySentiment.NEGATIVE -> Color(0xFFDC2626) // Dark red
        CategorySentiment.NEUTRAL -> Color(0xFF6B7280)  // Dark gray
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 카테고리 칩들을 가로로 나열하는 컴포넌트
 *
 * 최대 표시 개수를 제한하고, 초과하는 경우 "+N" 표시를 추가합니다.
 *
 * @param categories 표시할 카테고리 목록
 * @param maxVisible 최대 표시 개수 (기본값: 3)
 * @param modifier Modifier
 */
@Composable
fun CategoryChipRow(
    categories: List<MemoCategory>,
    maxVisible: Int = 3,
    modifier: Modifier = Modifier
) {
    val visibleCategories = categories.take(maxVisible)
    val remainingCount = (categories.size - maxVisible).coerceAtLeast(0)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        visibleCategories.forEach { category ->
            CategoryChip(category = category)
        }
        if (remainingCount > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE5E7EB)
            ) {
                Text(
                    text = "+$remainingCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * CategoryChip 프리뷰
 */
@Preview(showBackground = true, name = "Category Chips")
@Composable
private fun CategoryChipPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Positive:", style = MaterialTheme.typography.labelMedium)
            CategoryChip(
                category = MemoCategory(1, "너무 좋았다", CategorySentiment.POSITIVE, "#10B981", 1)
            )

            Text("Negative:", style = MaterialTheme.typography.labelMedium)
            CategoryChip(
                category = MemoCategory(6, "완전 최악", CategorySentiment.NEGATIVE, "#EF4444", 6)
            )

            Text("Neutral:", style = MaterialTheme.typography.labelMedium)
            CategoryChip(
                category = MemoCategory(9, "무난했다", CategorySentiment.NEUTRAL, "#6B7280", 9)
            )
        }
    }
}

/**
 * CategoryChipRow 프리뷰 - 3개 표시
 */
@Preview(showBackground = true, name = "Category Chip Row - 3 items")
@Composable
private fun CategoryChipRowPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("3 categories:", style = MaterialTheme.typography.labelMedium)
            CategoryChipRow(
                categories = listOf(
                    MemoCategory(1, "너무 좋았다", CategorySentiment.POSITIVE, "#10B981", 1),
                    MemoCategory(4, "분위기가 좋다", CategorySentiment.POSITIVE, "#10B981", 4),
                    MemoCategory(9, "무난했다", CategorySentiment.NEUTRAL, "#6B7280", 9)
                )
            )
        }
    }
}

/**
 * CategoryChipRow 프리뷰 - 5개 (3개 표시 + "+2")
 */
@Preview(showBackground = true, name = "Category Chip Row - 5 items (overflow)")
@Composable
private fun CategoryChipRowOverflowPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("5 categories (max 3 visible):", style = MaterialTheme.typography.labelMedium)
            CategoryChipRow(
                categories = listOf(
                    MemoCategory(1, "너무 좋았다", CategorySentiment.POSITIVE, "#10B981", 1),
                    MemoCategory(2, "다음에 또 방문", CategorySentiment.POSITIVE, "#10B981", 2),
                    MemoCategory(3, "가성비 좋다", CategorySentiment.POSITIVE, "#10B981", 3),
                    MemoCategory(6, "완전 최악", CategorySentiment.NEGATIVE, "#EF4444", 6),
                    MemoCategory(9, "무난했다", CategorySentiment.NEUTRAL, "#6B7280", 9)
                ),
                maxVisible = 3
            )
        }
    }
}
