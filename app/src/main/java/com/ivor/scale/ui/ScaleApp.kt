package com.ivor.scale.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

private enum class Screen { Home, RateWeight, Gold }

private val TABS = listOf("Weight", "Rate")

/** Top-level navigator. Lightweight state-based routing for three destinations. */
@Composable
fun ScaleApp(vm: ScaleViewModel = viewModel()) {
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    val animationsEnabled = rememberAnimationsEnabled()

    BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            if (!animationsEnabled) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                val forward = targetState.ordinal > initialState.ordinal
                val dir = if (forward) 1 else -1
                (slideInHorizontally { w -> dir * w } + fadeIn())
                    .togetherWith(slideOutHorizontally { w -> -dir * w } + fadeOut())
            }
        },
        label = "screen",
    ) { current ->
        when (current) {
            Screen.Home -> HomeScreen(
                onOpenRateWeight = { screen = Screen.RateWeight },
                onOpenGold = { screen = Screen.Gold },
            )

            Screen.RateWeight -> RateWeightScreen(
                vm = vm,
                animationsEnabled = animationsEnabled,
                onBack = { screen = Screen.Home },
            )

            Screen.Gold -> GoldScreen(
                vm = vm,
                onBack = { screen = Screen.Home },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RateWeightScreen(
    vm: ScaleViewModel,
    animationsEnabled: Boolean,
    onBack: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { TABS.size })
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

    val onCopy: (String) -> Unit = { value ->
        clipboard.setText(AnnotatedString(value))
        scope.launch { snackbarHostState.showSnackbar("Copy ho gaya: $value") }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rate & Weight",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wapas")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                TABS.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(text = title, style = MaterialTheme.typography.titleMedium)
                        },
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> WeightTab(vm, animationsEnabled, onCopy)
                    else -> RateTab(vm, animationsEnabled, onCopy)
                }
            }
        }
    }
}
