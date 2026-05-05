package com.redred.mapmyshots.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.MapMyShotsColors
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.cancel
import mapmyshots.composeapp.generated.resources.delete
import mapmyshots.composeapp.generated.resources.ignore_photo_action
import mapmyshots.composeapp.generated.resources.ignore_photo_message
import mapmyshots.composeapp.generated.resources.ignore_photo_title
import mapmyshots.composeapp.generated.resources.restore_photo_action
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PhotoListAssetActionsDialog(
    isIgnored: Boolean,
    onDismiss: () -> Unit,
    onToggleIgnored: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.ignore_photo_title)) },
        text = { Text(stringResource(Res.string.ignore_photo_message)) },
        confirmButton = {
            TextButton(onClick = onToggleIgnored) {
                Text(
                    stringResource(
                        if (isIgnored) Res.string.restore_photo_action else Res.string.ignore_photo_action
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
        icon = {
            TextButton(onClick = onDelete) {
                Text(stringResource(Res.string.delete))
            }
        },
        containerColor = MapMyShotsColors.background,
    )
}

@Preview
@Composable
private fun PhotoListAssetActionsDialogPreview() {
    MaterialTheme {
        PhotoListAssetActionsDialog(
            isIgnored = false,
            onDismiss = {},
            onToggleIgnored = {},
            onDelete = {}
        )
    }
}
