package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.MapMyShotsSizes

@Composable
internal fun FullScreenLoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(MapMyShotsSizes.listLoading))
    }
}

@Composable
internal fun InlineLoadingState(
    modifier: Modifier = Modifier,
    indicatorSize: androidx.compose.ui.unit.Dp = MapMyShotsSizes.loadMore
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(indicatorSize))
    }
}

@Composable
internal fun DetailsLoadingState() {
    InlineLoadingState(
        modifier = Modifier
            .fillMaxWidth()
            .height(MapMyShotsSizes.detailsLoadingHeight)
    )
}

@Preview
@Composable
private fun InlineLoadingStatePreview() {
    MaterialTheme {
        InlineLoadingState(modifier = Modifier.fillMaxWidth())
    }
}
