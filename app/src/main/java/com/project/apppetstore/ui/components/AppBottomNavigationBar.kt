package com.project.apppetstore.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.project.apppetstore.navigation.AppDestination
import com.project.apppetstore.navigation.BottomDestinations
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun AppBottomNavigationBar(
    currentRoute: String?,
    onTabSelected: (AppDestination) -> Unit
) {
    NavigationBar {
        BottomDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onTabSelected(destination) },
                icon = {
                    when {
                        destination.iconRes != null -> Icon(
                            painter = painterResource(id = destination.iconRes),
                            contentDescription = stringResource(destination.titleRes)
                        )
                    }
                },
                label = { Text(text = stringResource(destination.titleRes)) }
            )
        }
    }
}
