package com.redred.mapmyshots.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.error_title
import mapmyshots.composeapp.generated.resources.ok
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.error_title)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}

@Preview
@Composable
private fun ErrorDialogPreview() {
    MaterialTheme {
        ErrorDialog(
            message = "Something went wrong.",
            onDismiss = {}
        )
    }
}
