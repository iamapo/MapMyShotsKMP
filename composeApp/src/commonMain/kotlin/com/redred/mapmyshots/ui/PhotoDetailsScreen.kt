package com.redred.mapmyshots.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.viewmodel.PhotoDetailsViewModel
import com.seiko.imageloader.rememberImagePainter
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailsScreen(photo: Asset, onSaved: () -> Unit) {

    val vm: PhotoDetailsViewModel = koinInject(parameters = { parametersOf(photo) })

    val timeRange by vm.timeRange.collectAsState()
    val loading by vm.loading.collectAsState()
    val similar by vm.similar.collectAsState()
    val names by vm.locationNames.collectAsState()

    LaunchedEffect(photo.id) { vm.loadSimilar() }

    val scope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text("Photo Details") }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(12.dp)) {
            Card(Modifier.fillMaxWidth().aspectRatio(1f)) {
                val painter = rememberImagePainter(photo.uri)
                Image(painter, null, contentScale = ContentScale.Crop)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                SegmentedButtons(
                    options = listOf("1 hour","4 hours","12 hours"),
                    selected = timeRange,
                    onSelected = vm::setTimeRange
                )
            }
            Spacer(Modifier.height(16.dp))
            if (loading) Box(Modifier.fillMaxWidth()) { CircularProgressIndicator() }
            else {
                LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(similar, key = { it.id }) { a ->
                        Card(Modifier.clickable {
                            scope.launch {
                                val ok = vm.applyLocationFrom(a)
                                if (ok) onSaved()
                            }
                        }) {
                            Column {
                                val painter = rememberImagePainter(a.uri)
                                Image(painter, null, modifier = Modifier.height(160.dp).fillMaxWidth(), contentScale = ContentScale.Crop)
                                Text(names[a.id] ?: "", modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedButtons(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach {
            FilterChip(selected = it == selected, onClick = { onSelected(it) }, label = { Text(it) })
        }
    }
}