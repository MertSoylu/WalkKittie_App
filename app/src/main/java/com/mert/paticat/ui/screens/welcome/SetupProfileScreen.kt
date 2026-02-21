package com.mert.paticat.ui.screens.welcome

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupProfileScreen(
    onSetupComplete: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var catName by remember { mutableStateOf("") }
    var stepGoal by remember { mutableIntStateOf(6000) }
    var waterGoal by remember { mutableIntStateOf(2000) }
    var calorieGoal by remember { mutableIntStateOf(2000) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.setup_title), 
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface 
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EntranceAnimation {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.setup_subtitle),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Name Input Section
                EntranceAnimation(delay = 100) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.setup_label_name),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.setup_placeholder_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PremiumPink,
                                cursorColor = PremiumPink
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.setup_label_cat_name),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = catName,
                            onValueChange = { catName = it },
                            placeholder = { Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.setup_placeholder_cat_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PremiumBlue,
                                cursorColor = PremiumBlue
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Goals List
                EntranceAnimation(delay = 300) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.setup_label_goals),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        SetupGoalItem(
                            title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.goal_steps_title),
                            value = stepGoal,
                            unit = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.unit_steps),
                            step = 1000,
                            color = PremiumPink,
                            onValueChange = { stepGoal = it }
                        )
                        
                        SetupGoalItem(
                            title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.goal_water_title),
                            value = waterGoal,
                            unit = "ml",
                            step = 250,
                            color = PremiumBlue,
                            onValueChange = { waterGoal = it }
                        )
                    }
                }
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Finish Button
                EntranceAnimation(delay = 400) {
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = context.getString(com.mert.paticat.R.string.setup_error_name)
                            } else if (catName.isBlank()) {
                                errorMessage = context.getString(com.mert.paticat.R.string.setup_error_cat_name)
                            } else {
                                // Default gender set to MALE since we removed selection
                                viewModel.saveUserProfile(name, catName, "MALE", stepGoal, waterGoal, calorieGoal)
                                onSetupComplete()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .bounceClick {
                            if (name.isBlank()) {
                                errorMessage = context.getString(com.mert.paticat.R.string.setup_error_name)
                            } else if (catName.isBlank()) {
                                errorMessage = context.getString(com.mert.paticat.R.string.setup_error_cat_name)
                            } else {
                                viewModel.saveUserProfile(name, catName, "MALE", stepGoal, waterGoal, calorieGoal)
                                onSetupComplete()
                            }
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PremiumPink,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.setup_btn_start),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SetupGoalItem(
    title: String,
    value: Int,
    unit: String,
    step: Int,
    color: Color,
    onValueChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha=0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$value",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelLarge,
                        color = color.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (value > step) onValueChange(value - step) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .bounceClick { if (value > step) onValueChange(value - step) }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = color)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                IconButton(
                    onClick = { onValueChange(value + step) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .bounceClick { onValueChange(value + step) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = color)
                }
            }
        }
    }
}
