package com.seniorsafe.feature.guardian

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seniorsafe.core.model.FallHistoryItem
import com.seniorsafe.core.ui.theme.Danger500
import com.seniorsafe.core.ui.theme.Neutral400
import com.seniorsafe.core.ui.theme.Neutral600
import com.seniorsafe.core.ui.theme.Neutral900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FallHistoryScreen(
    seniorName: String,
    onBack: () -> Unit,
    viewModel: FallHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${seniorName}님 낙상 이력") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(uiState.events) { event ->
                FallHistoryItemCard(event = event)
            }
        }
    }
}

@Composable
private fun FallHistoryItemCard(event: FallHistoryItem) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(12.dp).clip(CircleShape),
                color    = if (event.cancelled) Neutral400 else Danger500,
                shape    = CircleShape
            ) {}
            Spacer(Modifier.width(16.dp))
            Column {
                Text(event.detectedAt, fontSize = 14.sp, color = Neutral900)
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = if (event.cancelled) "본인 취소" else "알림 전송됨",
                    fontSize = 12.sp,
                    color    = Neutral600
                )
            }
        }
    }
}
