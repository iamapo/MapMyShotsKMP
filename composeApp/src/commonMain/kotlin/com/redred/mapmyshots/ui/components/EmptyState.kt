package com.redred.mapmyshots.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.MapMyShotsColors
import com.redred.mapmyshots.ui.theme.MapMyShotsTypography
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.no_similar_photos
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SimilarPhotosEmptyState() {
    Text(
        text = stringResource(Res.string.no_similar_photos),
        fontSize = MapMyShotsTypography.metadata,
        color = MapMyShotsColors.textSecondary
    )
}

@Preview
@Composable
private fun SimilarPhotosEmptyStatePreview() {
    MaterialTheme {
        SimilarPhotosEmptyState()
    }
}
