package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GymPrimaryIndigo
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material3.Divider


@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) GymPrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FilterDropdownChip(
    label: String,
    options: Map<String, String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var dropDownWidth by remember { mutableStateOf(0) }
    var dropDownHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevronAnim")

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            dropDownWidth = coordinates.size.width
            dropDownHeight = coordinates.size.height
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    if (expanded) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                    else RoundedCornerShape(24.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp).rotate(rotation),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        if (expanded && dropDownWidth > 0 && dropDownHeight > 0) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, dropDownHeight),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Column(
                    modifier = Modifier
                        .width(with(density) { dropDownWidth.toDp() })
                        .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp)
                    options.forEach { (key, valLabel) ->
                        Text(
                            text = valLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemSelected(key)
                                    expanded = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
