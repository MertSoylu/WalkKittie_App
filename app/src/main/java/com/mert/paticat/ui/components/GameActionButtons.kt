package com.mert.paticat.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mert.paticat.R

/**
 * Reusable Play Again / Close button row used in game result states.
 */
@Composable
fun GameActionButtons(
    accentColor: androidx.compose.ui.graphics.Color,
    onPlayAgain: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onPlayAgain,
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier.weight(1.3f).height(50.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.btn_play_again),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.btn_close),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
