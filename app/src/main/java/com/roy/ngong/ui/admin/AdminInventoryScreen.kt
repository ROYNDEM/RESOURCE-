package com.roy.ngong.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.roy.ngong.data.SundayResourceInventory
import com.roy.ngong.ui.resource.viewmodel.ResourceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInventoryScreen(
    resourceViewModel: ResourceViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var dateString by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var biscuitBoxes by remember { mutableStateOf("") }
    var printingReams by remember { mutableStateOf("") }
    
    val inventoryRecords by resourceViewModel.inventoryRecords.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        resourceViewModel.startListeningForInventory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sunday Inventory") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD32F2F), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Entry Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Record New Sunday Entry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = biscuitBoxes,
                            onValueChange = { if (it.all { char -> char.isDigit() }) biscuitBoxes = it },
                            label = { Text("Biscuit Boxes") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = printingReams,
                            onValueChange = { if (it.all { char -> char.isDigit() }) printingReams = it },
                            label = { Text("Printing Reams") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (biscuitBoxes.isNotEmpty() && printingReams.isNotEmpty()) {
                                val record = SundayResourceInventory(
                                    date = dateString,
                                    biscuitBoxes = biscuitBoxes.toInt(),
                                    printingReams = printingReams.toInt(),
                                    recordedBy = authViewModel.currentUser?.email ?: "Unknown Admin"
                                )
                                resourceViewModel.saveInventoryRecord(record, 
                                    onSuccess = {
                                        biscuitBoxes = ""
                                        printingReams = ""
                                    },
                                    onFailure = { /* Handle error */ }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Save Record")
                    }
                }
            }

            Text("Previous Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(inventoryRecords) { record ->
                    InventoryRecordItem(record)
                }
            }
        }
    }
}

@Composable
fun InventoryRecordItem(record: SundayResourceInventory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(record.date, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text("By: ${record.recordedBy.split("@")[0]}", style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Biscuit Boxes: ${record.biscuitBoxes}")
                Text("Printing Reams: ${record.printingReams}")
            }
        }
    }
}