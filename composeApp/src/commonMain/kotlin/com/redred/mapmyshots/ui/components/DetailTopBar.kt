package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.ui.theme.*
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.details_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DetailTopBar(
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null,
    isIgnored: Boolean = false,
    onToggleIgnored: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MapMyShotsSizes.detailTopBarHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val edgeOffset = (MapMyShotsSizes.detailBackButton - 24.dp) / 2

        Box(
            modifier = Modifier
                .size(MapMyShotsSizes.detailBackButton)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MapMyShotsColors.primary
            )
        }

        Spacer(Modifier.width(MapMyShotsSpacing.md))

        Text(
            text = stringResource(Res.string.details_title),
            fontSize = MapMyShotsTypography.detailTitle,
            fontWeight = FontWeight.Bold,
            color = MapMyShotsColors.textPrimary
        )

        Spacer(Modifier.weight(1f))

        if (onToggleIgnored != null) {
            Box(
                modifier = Modifier
                    .size(MapMyShotsSizes.detailBackButton)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleIgnored),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIgnored) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = MapMyShotsColors.primary,
                )
            }
            Spacer(Modifier.width(MapMyShotsSpacing.xs))
        }

        if (onDelete != null) {
            Box(
                modifier = Modifier
                    .size(MapMyShotsSizes.detailBackButton)
                    .clip(CircleShape)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MapMyShotsColors.primary,
                )
            }
        }
    }
}

@Preview
@Composable
private fun DetailTopBarPreview() {
    MaterialTheme {
        DetailTopBar(onBack = {}, onDelete = {}, onToggleIgnored = {})
    }
}
