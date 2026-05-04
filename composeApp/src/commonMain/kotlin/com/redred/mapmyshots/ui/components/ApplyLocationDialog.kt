package com.redred.mapmyshots.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.MapMyShotsColors
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.apply
import mapmyshots.composeapp.generated.resources.apply_location_message
import mapmyshots.composeapp.generated.resources.apply_location_title
import mapmyshots.composeapp.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ApplyLocationDialog(
    sourceName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.apply_location_title)) },
        text = { Text(stringResource(Res.string.apply_location_message, sourceName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
        containerColor = MapMyShotsColors.background
        )
}

@Preview
@Composable
private fun ApplyLocationDialogPreview() {
    MaterialTheme {
        ApplyLocationDialog(
            sourceName = "Munich · Marienplatz",
            onDismiss = {},
            onConfirm = {}
        )
    }
}
