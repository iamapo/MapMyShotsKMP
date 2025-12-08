package com.redred.mapmyshots.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.redred.mapmyshots.model.Asset

@Composable
expect fun AssetThumbnail(
    asset: Asset,
    modifier: Modifier = Modifier
)