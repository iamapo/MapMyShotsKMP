package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.MapMyShotsColors
import com.redred.mapmyshots.ui.theme.MapMyShotsSpacing
import com.redred.mapmyshots.ui.theme.MapMyShotsTypography
import com.redred.mapmyshots.viewmodel.PhotoListTab
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.empty_ignored_photos
import mapmyshots.composeapp.generated.resources.empty_review_photos
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyPhotoListState(selectedTab: PhotoListTab) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(
                if (selectedTab == PhotoListTab.Review) {
                    Res.string.empty_review_photos
                } else {
                    Res.string.empty_ignored_photos
                }
            ),
            modifier = Modifier.padding(MapMyShotsSpacing.xl),
            fontSize = MapMyShotsTypography.metadata,
            color = MapMyShotsColors.textSecondary
        )
    }
}

@Preview
@Composable
private fun EmptyPhotoListStatePreview() {
    MaterialTheme {
        EmptyPhotoListState(selectedTab = PhotoListTab.Review)
    }
}
