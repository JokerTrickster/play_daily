package com.dailymemo.presentation.memo.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailymemo.domain.models.Comment
import java.time.format.DateTimeFormatter

@Composable
fun CommentsSection(
    comments: List<Comment>,
    commentInput: String,
    commentRating: Int,
    onCommentInputChange: (String) -> Unit,
    onCommentRatingChange: (Int) -> Unit,
    onPostComment: () -> Unit,
    onDeleteComment: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "댓글 (${comments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 댓글 입력
            CommentInputSection(
                commentInput = commentInput,
                commentRating = commentRating,
                onCommentInputChange = onCommentInputChange,
                onCommentRatingChange = onCommentRatingChange,
                onPostComment = onPostComment
            )

            if (comments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                // 댓글 목록
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(comments) { comment ->
                        CommentItem(
                            comment = comment,
                            onDelete = { onDeleteComment(comment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentInputSection(
    commentInput: String,
    commentRating: Int,
    onCommentInputChange: (String) -> Unit,
    onCommentRatingChange: (Int) -> Unit,
    onPostComment: () -> Unit
) {
    Column {
        // 평점 입력
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "평점:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
            repeat(5) { index ->
                IconButton(
                    onClick = { onCommentRatingChange(index + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (index < commentRating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "${index + 1}점",
                        tint = if (index < commentRating) Color(0xFFFFB800) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (commentRating > 0) {
                TextButton(onClick = { onCommentRatingChange(0) }) {
                    Text("초기화", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 댓글 입력
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentInput,
                onValueChange = onCommentInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("댓글을 입력하세요") },
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onPostComment,
                enabled = commentInput.isNotBlank()
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "댓글 작성",
                    tint = if (commentInput.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onDelete: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.userName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = comment.createdAt.format(timeFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                // 댓글 평점 표시
                if (comment.rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < comment.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (index < comment.rating) Color(0xFFFFB800) else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "댓글 삭제",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
