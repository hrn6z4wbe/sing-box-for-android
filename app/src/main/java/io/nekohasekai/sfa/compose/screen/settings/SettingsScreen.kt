package io.nekohasekai.sfa.compose.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.nekohasekai.sfa.R
import io.nekohasekai.sfa.compose.topbar.OverrideTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    OverrideTopBar { TopAppBar(title = { Text(stringResource(R.string.title_settings)) }) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column {
                SettingsEntry(
                    title = stringResource(R.string.title_app_settings),
                    icon = { Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    onClick = { navController.navigate("settings/app") },
                )
                SettingsEntry(
                    title = stringResource(R.string.core),
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = { navController.navigate("settings/core") },
                )
                SettingsEntry(
                    title = stringResource(R.string.service),
                    icon = { Icon(Icons.Outlined.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = { navController.navigate("settings/service") },
                )
                SettingsEntry(
                    title = stringResource(R.string.profile_override),
                    icon = { Icon(Icons.Outlined.FilterAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
                    onClick = { navController.navigate("settings/profile_override") },
                )
            }
        }
    }
}

@Composable
private fun SettingsEntry(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        leadingContent = icon,
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
