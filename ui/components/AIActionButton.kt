package com.aktarjabed.androphoshop.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AIActionButton(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(horizontalAlignment = Alignment.End) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FloatingActionButton(
                        onClick = { navController.navigate("editor") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) { Icon(Icons.Default.AutoAwesome, null) }

                    FloatingActionButton(
                        onClick = { navController.navigate("editor") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) { Icon(Icons.Default.BackgroundRemove, null) }
                }
            }

            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    if (expanded) Icons.Default.Close else Icons.Default.AutoAwesome,
                    null
                )
            }
        }
    }
}