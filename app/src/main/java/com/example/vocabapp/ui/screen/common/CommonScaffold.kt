package com.example.vocabapp.ui.screen.common

import com.example.vocabapp.ui.theme.TextMuted

import com.example.vocabapp.ui.theme.BrightBlue

import com.example.vocabapp.ui.theme.DeepBlue
import com.example.vocabapp.ui.theme.Gold
import com.example.vocabapp.ui.theme.HeaderGreen
import com.example.vocabapp.ui.theme.SoftBlue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocabapp.R
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vocabapp.domain.model.ContentType
import com.example.vocabapp.ui.navigation.Route

internal val LocalGramNavController = compositionLocalOf<NavHostController?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BlueScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    val navController = LocalGramNavController.current
    Scaffold(
        containerColor = SoftBlue,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GramCraft", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, lineHeight = 22.sp)
                        Text(title, color = Color.White.copy(alpha = 0.82f), fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderGreen)
            )
        },
        bottomBar = {
            if (navController != null) {
                GramBottomNavigation(navController = navController)
            }
        },
        content = content
    )
}

@Composable
internal fun GramBottomNavigation(
    navController: NavHostController,
    activeRoute: String? = null
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = activeRoute ?: backStackEntry?.destination?.route.orEmpty()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SoftBlue)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (currentRoute == Route.Home.path) {
            Image(
                painter = painterResource(R.drawable.gram_home_robot_cutout),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-18).dp)
                    .size(48.dp),
                contentScale = ContentScale.Fit
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GramBottomItem(
                label = "ホーム",
                icon = Icons.Default.Home,
                active = currentRoute == Route.Home.path,
                onClick = {
                    navController.navigate(Route.Home.path) {
                        launchSingleTop = true
                        popUpTo(Route.Home.path)
                    }
                }
            )
            GramBottomItem(
                label = "レッスン",
                icon = Icons.Default.School,
                active = currentRoute.startsWith(Route.CustomTraining.PATTERN.substringBefore("/{")),
                onClick = { navController.navigate(Route.customTraining(ContentType.WORD.routeValue)) }
            )
            GramBottomItem("復習", Icons.Default.Refresh, currentRoute == Route.Review.path) {
                navController.navigate(Route.Review.path)
            }
            GramBottomItem("学習ログ", Icons.AutoMirrored.Filled.FormatListBulleted, currentRoute == Route.StudyLog.path) {
                navController.navigate(Route.StudyLog.path)
            }
            GramBottomItem("設定", Icons.Default.Settings, currentRoute == Route.Settings.path) {
                navController.navigate(Route.Settings.path)
            }
        }
    }
}

@Composable
private fun GramBottomItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(if (active) BrightBlue.copy(alpha = 0.16f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = if (active) BrightBlue else TextMuted, modifier = Modifier.size(21.dp))
        Text(label, color = if (active) BrightBlue else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(value, color = DeepBlue, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
internal fun CardButton(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (title.contains("復習")) Gold.copy(alpha = 0.22f) else BrightBlue.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = BrightBlue, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = DeepBlue, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = TextMuted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
internal fun BottomAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(54.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBlue)) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.Bold)
    }
}
