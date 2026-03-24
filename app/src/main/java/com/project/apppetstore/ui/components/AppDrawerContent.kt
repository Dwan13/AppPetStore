package com.project.adopetshop.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.project.adopetshop.navigation.AppDestination
import com.project.adopetshop.navigation.BottomDestinations

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    onDestinationSelected: (AppDestination) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color(0xFF5C9639),
                            androidx.compose.ui.graphics.Color(0xFF7EBB56)
                        )
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
        )
        Text(
            text = "AdoPetShop",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        BottomDestinations.forEach { destination ->
            NavigationDrawerItem(
                label = { Text(stringResource(destination.titleRes)) },
                selected = currentRoute == destination.route,
                onClick = { onDestinationSelected(destination) }
            )
        }
    }
}
