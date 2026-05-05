package com.redred.mapmyshots.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object MapMyShotsColors {
    val primary = Color(0xFF0B63E5)
    val background = Color.White
    val surface = Color.White
    val surfaceSelected = primary.copy(alpha = 0.06f)
    val transparent = Color.Transparent
    val onImage = Color.White
    val textPrimary = Color(0xFF101828)
    val textSecondary = Color(0xFF667085)
    val textMuted = Color(0xFF557099)
    val textValue = Color(0xFF475467)
    val border = Color(0xFFD0D5DD)
    val divider = Color(0xFFE4E7EC)
    val badgeBackground = Color(0xCC344054)
    val imageOverlay = Color.Black.copy(alpha = 0.12f)
    val success = Color(0xFF16803C)
    val successAccent = Color(0xFF2EEA7A)
    val successBackground = Color(0xFFEAF8EF)
    val successBorder = Color(0xFFB7E4C7)
}

internal object MapMyShotsSpacing {
    val xxs = 4.dp
    val xs = 6.dp
    val sm = 8.dp
    val md = 10.dp
    val lg = 12.dp
    val xl = 14.dp
    val xxl = 16.dp
    val screen = 20.dp
    val bottomSpacer = 24.dp
}

internal object MapMyShotsSizes {
    val galleryMinCell = 164.dp
    val listLoading = 40.dp
    val loadMore = 28.dp
    val photoCardImageHeight = 138.dp
    val selectedBadge = 34.dp
    val detailTopBarHeight = 48.dp
    val detailBackButton = 42.dp
    val detailHeroHeight = 270.dp
    val detailsLoadingHeight = 96.dp
    val iconButton = 44.dp
    val metadataRowHeight = 52.dp
    val metadataIconWidth = 34.dp
    val suggestionHeight = 108.dp
    val suggestionImageWidth = 104.dp
    val suggestionActionWidth = 68.dp
    val successIcon = 22.dp
    val timeSelectorHeight = 46.dp
    val thumbnailPreviewHeight = 180.dp
}

internal object MapMyShotsStroke {
    val thin = 1.dp
    val medium = 1.5.dp
    val selected = 2.dp
}

internal object MapMyShotsShapes {
    val sm = RoundedCornerShape(16.dp)
    val card = RoundedCornerShape(18.dp)
    val suggestionImage = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
    val metadataCard = RoundedCornerShape(22.dp)
    val hero = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(50)
}

internal object MapMyShotsTypography {
    val badge = 14.sp
    val caption = 12.sp
    val imageLabel = 13.sp
    val metadata = 16.sp
    val cardDate = 15.sp
    val gallerySubtitle = 17.sp
    val suggestionIcon = 18.sp
    val suggestionTitle = 17.sp
    val metadataIcon = 21.sp
    val sectionTitle = 22.sp
    val detailTitle = 25.sp
    val iconButton = 28.sp
    val heroTitle = 32.sp
    val backArrow = 32.sp
}
