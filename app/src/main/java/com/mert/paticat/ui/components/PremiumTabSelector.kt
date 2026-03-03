package com.mert.paticat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PremiumTabSelector(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, text ->
            val isSelected = selectedIndex == index
            val bgColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "tab_bg"
            )
            val textColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "tab_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(bgColor)
                    .clickable { onSelect(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = if (options.size > 3) 12.sp else 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
