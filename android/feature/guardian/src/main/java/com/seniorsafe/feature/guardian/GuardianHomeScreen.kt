package com.seniorsafe.feature.guardian

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seniorsafe.core.model.PairingItem
import com.seniorsafe.core.ui.theme.Neutral400
import com.seniorsafe.core.ui.theme.Neutral600
import com.seniorsafe.core.ui.theme.Success500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardianHomeScreen(
    onNavigateToConnect: () -> Unit,
    onNavigateToFallHistory: (seniorId: String, seniorName: String) -> Unit,
    viewModel: GuardianHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SeniorSafe") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("어르신 연결하기") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onNavigateToConnect
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
            items(uiState.pairings) { item ->
                SeniorCard(
                    item    = item,
                    onClick = { onNavigateToFallHistory(item.seniorId, item.seniorName) }
                )
            }
            if (uiState.pairings.isEmpty() && !uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                        Text("연결된 어르신이 없어요", color = Neutral600)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeniorCard(item: PairingItem, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.seniorName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .then(
                                if (item.serviceActive)
                                    Modifier.padding(0.dp)
                                else
                                    Modifier.padding(0.dp)
                            )
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color    = if (item.serviceActive) Success500 else Neutral400,
                            shape    = CircleShape
                        ) {}
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text     = if (item.active) "연결됨" else "연결 해제됨",
                        fontSize = 14.sp,
                        color    = Neutral600
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = if (item.lastActivityAt != null) "마지막 사용 기록: ${item.lastActivityAt}" else "마지막 사용 기록: 없음",
                    fontSize = 12.sp,
                    color    = Neutral400
                )
            }
            Text("›", fontSize = 22.sp, color = Neutral400)
        }
    }
}
