package com.personal.lifeos.ui.screens.health

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.lifeos.service.GoogleFitManager
import com.personal.lifeos.ui.components.BouncingDotsLoader
import com.personal.lifeos.ui.components.InfiniteAuraRing
import com.personal.lifeos.ui.theme.*
import com.personal.lifeos.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthStepsScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val googleFitManager = remember { homeViewModel.googleFitManager }
    var isFitConnected by remember { mutableStateOf(googleFitManager.isConnected()) }
    var isSyncing by remember { mutableStateOf(false) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var currentWeight by remember { mutableStateOf(79.5) }
    var weightInput by remember { mutableStateOf("79.5") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        scope.launch {
            if (grantedPermissions.containsAll(GoogleFitManager.PERMISSIONS)) {
                googleFitManager.setConnected(true)
                isFitConnected = true
                isSyncing = true
                homeViewModel.syncGoogleFit { success, msg ->
                    isSyncing = false
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Google Fit permissions not granted.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto-sync actual steps from Google Fit when opening screen if connected
    LaunchedEffect(Unit) {
        if (googleFitManager.isConnected()) {
            isSyncing = true
            homeViewModel.syncGoogleFit { _, _ ->
                isSyncing = false
            }
        }
    }

    val stepPct = if (state.stepsTarget > 0) (state.stepsCurrent.toFloat() / state.stepsTarget).coerceIn(0f, 1f) else 0f
    val distanceKm = state.stepsCurrent * 0.00075
    val caloriesBurned = state.stepsCurrent * 0.04
    val activeMinutes = state.stepsCurrent / 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Health & Step Activity",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDarkPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { showSetupDialog = true }) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = "Google Fit Help",
                            tint = SkyBluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBackground)
            )
        },
        containerColor = SkyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GOOGLE FIT REAL INTEGRATION CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(22.dp), ambientColor = GlowSkyBlue),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SkyBlueSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = "Google Fit",
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Google Fit Integration",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextDarkPrimary
                                )
                                Text(
                                    text = if (isFitConnected) "🟢 Connected to Google Fit" else "⚪ Not Connected",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isFitConnected) EmeraldSuccess else TextMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = { googleFitManager.openGoogleFitApp() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = "Open Google Fit App",
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isFitConnected) {
                            val lastSync = googleFitManager.getLastSyncTime()
                            val timeStr = if (lastSync > 0) {
                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(lastSync))
                            } else "Just now"
                            "Live sync active with Google Fit. Actual steps update whenever you sync or walk."
                        } else {
                            "Connect Google Fit to automatically synchronize your actual real-world steps and activity from your phone and smartwatch."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDarkSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isFitConnected) {
                            Button(
                                onClick = {
                                    if (googleFitManager.isHealthConnectAvailable()) {
                                        try {
                                            permissionLauncher.launch(GoogleFitManager.PERMISSIONS)
                                        } catch (e: Exception) {
                                            showSetupDialog = true
                                        }
                                    } else {
                                        showSetupDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = "Connect", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connect Google Fit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    isSyncing = true
                                    // Fetch actual live steps from Google Fit / Health Connect API
                                    homeViewModel.syncGoogleFit { success, msg ->
                                        isSyncing = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                                modifier = Modifier.weight(1f).height(44.dp),
                                enabled = !isSyncing
                            ) {
                                if (isSyncing) {
                                    BouncingDotsLoader(dotSize = 6.dp, color = PureWhite)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync Actual Steps", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { showSetupDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(SkyBluePrimary, SkyBlueVibrant))),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SkyBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Setup", color = SkyBluePrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ACTUAL STEPS PROGRESS CARD (REAL HARDWARE + GOOGLE FIT)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = GlowSkyBlue),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isFitConnected) EmeraldSuccess else SkyBluePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFitConnected) "GOOGLE FIT LIVE SYNC" else "HARDWARE PEDOMETER ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = SkyBluePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    InfiniteAuraRing(
                        progress = stepPct,
                        size = 170.dp,
                        strokeWidth = 14.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%,d", state.stepsCurrent),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkPrimary,
                                fontSize = 34.sp
                            )
                            Text(
                                text = "of ${String.format("%,d", state.stepsTarget)} steps",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SkyStatPill("Distance", String.format("%.2f km", distanceKm))
                        SkyStatPill("Calories", "${caloriesBurned.toInt()} kcal")
                        SkyStatPill("Active Time", "${activeMinutes} min")
                    }
                }
            }

            // WEIGHT & BODY METRIC CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, PureWhite)))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WEIGHT & TARGET",
                            style = MaterialTheme.typography.labelSmall,
                            color = SkyBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showWeightDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Weight", tint = SkyBluePrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$currentWeight kg",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDarkPrimary
                            )
                            Text("Current Weight", style = MaterialTheme.typography.labelSmall, color = TextDarkSecondary)
                        }

                        Icon(Icons.Default.ArrowForward, contentDescription = "to", tint = SkyBluePrimary)

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "75.0 kg",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldSuccess
                            )
                            Text("Goal Target", style = MaterialTheme.typography.labelSmall, color = TextDarkSecondary)
                        }
                    }
                }
            }
        }

        // GOOGLE FIT SETUP GUIDE MODAL
        if (showSetupDialog) {
            AlertDialog(
                onDismissRequest = { showSetupDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = SkyBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google Fit & Health Setup", fontWeight = FontWeight.ExtraBold, color = TextDarkPrimary)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Google Fit synchronizes actual steps directly with Life OS via Android Health Connect.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDarkSecondary
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SkyBlueSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Setup Steps:",
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBluePrimary,
                                    fontSize = 13.sp
                                )
                                Text("1. Open Google Fit app on your device.", fontSize = 12.sp, color = TextDarkPrimary)
                                Text("2. Go to Profile -> Settings -> enable 'Sync Fit with Health Connect'.", fontSize = 12.sp, color = TextDarkPrimary)
                                Text("3. Tap below to grant permission to read your actual steps.", fontSize = 12.sp, color = TextDarkPrimary)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { googleFitManager.openGoogleFitApp() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Open Fit App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { googleFitManager.openHealthConnectSettings() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Health Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (googleFitManager.isHealthConnectAvailable()) {
                                    try {
                                        permissionLauncher.launch(GoogleFitManager.PERMISSIONS)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Please install Health Connect", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    googleFitManager.openHealthConnectSettings()
                                }
                                showSetupDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                        ) {
                            Text("Grant Health Permissions", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSetupDialog = false }) {
                        Text("Close", fontWeight = FontWeight.Bold, color = TextDarkPrimary)
                    }
                },
                containerColor = PureWhite
            )
        }

        // WEIGHT DIALOG
        if (showWeightDialog) {
            AlertDialog(
                onDismissRequest = { showWeightDialog = false },
                title = { Text("Log Current Weight", color = TextDarkPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            weightInput.toDoubleOrNull()?.let {
                                currentWeight = it
                            }
                            showWeightDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                    ) {
                        Text("Save Weight")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWeightDialog = false }) { Text("Cancel") }
                },
                containerColor = PureWhite
            )
        }
    }
}

@Composable
fun SkyStatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = TextDarkPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextDarkSecondary)
    }
}
