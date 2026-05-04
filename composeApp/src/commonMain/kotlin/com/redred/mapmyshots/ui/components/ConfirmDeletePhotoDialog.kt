package com.redred.mapmyshots.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.cancel
import mapmyshots.composeapp.generated.resources.delete
import mapmyshots.composeapp.generated.resources.delete_photo_message
import mapmyshots.composeapp.generated.resources.delete_photo_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ConfirmDeletePhotoDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_photo_title)) },
        text = { Text(stringResource(Res.string.delete_photo_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@Preview
@Composable
private fun ConfirmDeletePhotoDialogPreview() {
    MaterialTheme {
        ConfirmDeletePhotoDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}
