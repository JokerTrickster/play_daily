package com.dailymemo.presentation.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MapActionButtons(
    onMyLocationClick: () -> Unit,
    onCreateMemoClick: () -> Unit,
    canCreateMemo: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MyLocationButton(onClick = onMyLocationClick)
        CreateMemoButton(
            onClick = onCreateMemoClick,
            canCreateMemo = canCreateMemo
        )
    }
}

@Composable
fun MyLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            Icons.Filled.MyLocation,
            contentDescription = "내 위치",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CreateMemoButton(
    onClick: () -> Unit,
    canCreateMemo: Boolean,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = if (canCreateMemo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (canCreateMemo) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "메모 추가",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ToggleMemoListButton(
    showMemoList: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            Icons.Filled.List,
            contentDescription = "메모 목록",
            modifier = Modifier.size(20.dp)
        )
    }
}
