package com.convo.screens

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.convo.R
import com.convo.ui.components.ExitConfirmationDialog
import com.convo.ui.theme.AccentPrimary
import com.convo.ui.theme.BgPrimary
import com.convo.ui.theme.BgSecondary
import com.convo.ui.theme.BgTertiary
import com.convo.ui.theme.ConvoTheme
import com.convo.ui.theme.TextMuted
import com.convo.ui.theme.TextPrimary
import com.convo.ui.theme.TextSecondary
import kotlinx.coroutines.launch

class Dashboard : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConvoTheme(dynamicColor = false) {
                var showExitDialog by remember { mutableStateOf(false) }

                DisposableEffect(Unit) {
                    val callback = object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            showExitDialog = true
                        }
                    }
                    onBackPressedDispatcher.addCallback(callback)
                    onDispose { callback.remove() }
                }

                DashboardScreen(
                    showExitDialog = showExitDialog,
                    onDismissExitDialog = { showExitDialog = false },
                    onConfirmExit = {
                        finishAffinity()
                    }
                )
            }
        }
    }
}

data class NavItem(
    val title: String,
    val iconRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    showExitDialog: Boolean = false,
    onDismissExitDialog: () -> Unit = {},
    onConfirmExit: () -> Unit = {}
) {
    val navItems = listOf(
        NavItem("Chats", R.drawable.ic_chat),
        NavItem("Updates", R.drawable.ic_updates),
        NavItem("Communities", R.drawable.ic_communities),
        NavItem("Calls", R.drawable.ic_call)
    )

    val pagerState = rememberPagerState(initialPage = 0) { navItems.size }
    val coroutineScope = rememberCoroutineScope()

    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = onDismissExitDialog,
            onConfirm = onConfirmExit
        )
    }

    Scaffold(
        containerColor = BgPrimary,
        bottomBar = {
            NavigationBar(
                containerColor = BgSecondary,
                contentColor = TextPrimary
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentPrimary,
                            selectedTextColor = AccentPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = BgTertiary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> ChatsPage()
                1 -> UpdatesPage()
                2 -> CommunitiesPage()
                3 -> CallsPage()
            }
        }
    }
}

@Composable
fun ChatsPage() {
    var showMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Unread", "Favourites", "Groups")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Name
            Text(
                text = "Convo",
                color = AccentPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Right side icons
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* QR Scanner */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_qr_scanner),
                        contentDescription = "QR Scanner",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { /* Camera */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Camera",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more_vert),
                            contentDescription = "More Options",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(BgSecondary)
                    ) {
                        DropdownMenuItem(
                            text = { Text("New group", color = TextPrimary) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("New Community", color = TextPrimary) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("New broadcast", color = TextPrimary) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Linked devices", color = TextPrimary) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Starred messages", color = TextPrimary) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Read all", color = TextPrimary) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings", color = TextPrimary) },
                            onClick = { showMenu = false }
                        )
                    }
                }
            }
        }

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BgSecondary)
                .clickable { /* Open Search */ }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search",
                    color = TextMuted,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Options
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    text = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chats List (placeholder)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(10) { index ->
                ChatItem(
                    name = "User ${index + 1}",
                    lastMessage = "This is a sample message preview...",
                    time = "12:${30 + index} PM",
                    unreadCount = if (index % 3 == 0) index + 1 else 0
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) AccentPrimary else BgSecondary)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun ChatItem(
    name: String,
    lastMessage: String,
    time: String,
    unreadCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Open chat */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(BgTertiary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.first().toString(),
                color = AccentPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Message Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = time,
                    color = if (unreadCount > 0) AccentPrimary else TextMuted,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lastMessage,
                    color = TextMuted,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(AccentPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpdatesPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Updates",
                color = AccentPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Status updates will appear here",
                color = TextMuted,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CommunitiesPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Communities",
                color = AccentPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your communities will appear here",
                color = TextMuted,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CallsPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Calls",
                color = AccentPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your call history will appear here",
                color = TextMuted,
                fontSize = 14.sp
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    ConvoTheme(dynamicColor = false) {
        DashboardScreen()
    }
}
