package com.dailymemo.presentation.map.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dailymemo.R
import com.dailymemo.presentation.map.DistanceFilter
import com.dailymemo.presentation.map.WishlistFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistFilterChips(
    selectedFilter: WishlistFilter,
    onFilterSelected: (WishlistFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == WishlistFilter.ALL,
            onClick = { onFilterSelected(WishlistFilter.ALL) },
            label = { Text(stringResource(R.string.wishlist_filter_all)) }
        )
        FilterChip(
            selected = selectedFilter == WishlistFilter.WISHLIST_ONLY,
            onClick = { onFilterSelected(WishlistFilter.WISHLIST_ONLY) },
            label = { Text(stringResource(R.string.wishlist_filter_wishlist)) }
        )
        FilterChip(
            selected = selectedFilter == WishlistFilter.VISITED_ONLY,
            onClick = { onFilterSelected(WishlistFilter.VISITED_ONLY) },
            label = { Text(stringResource(R.string.wishlist_filter_visited)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistanceFilterChips(
    selectedFilter: DistanceFilter,
    onFilterSelected: (DistanceFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DistanceFilterChip(
            label = "전체",
            selected = selectedFilter == DistanceFilter.ALL,
            onClick = { onFilterSelected(DistanceFilter.ALL) }
        )
        DistanceFilterChip(
            label = "5km",
            selected = selectedFilter == DistanceFilter.WITHIN_5KM,
            onClick = { onFilterSelected(DistanceFilter.WITHIN_5KM) }
        )
        DistanceFilterChip(
            label = "10km",
            selected = selectedFilter == DistanceFilter.WITHIN_10KM,
            onClick = { onFilterSelected(DistanceFilter.WITHIN_10KM) }
        )
        DistanceFilterChip(
            label = "20km",
            selected = selectedFilter == DistanceFilter.WITHIN_20KM,
            onClick = { onFilterSelected(DistanceFilter.WITHIN_20KM) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistanceFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}
