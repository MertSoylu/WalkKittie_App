package com.mert.paticat.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mert.paticat.R
import com.mert.paticat.ui.components.PatiCatBackground
import com.mert.paticat.ui.components.marshmallow.ActionPillButton
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.pillowPress
import com.mert.paticat.ui.components.marshmallow.softEntrance
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupProfileScreen(
    onSetupComplete: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel(),
) {
    var name by rememberSaveable { mutableStateOf("") }
    var catName by rememberSaveable { mutableStateOf("") }
    var stepGoal by rememberSaveable { mutableIntStateOf(6000) }
    var waterGoal by rememberSaveable { mutableIntStateOf(2000) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val markNewUser: () -> Unit = remember {
        {
            context.getSharedPreferences("paticat_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean("tutorial_new_user", true)
                .apply()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PatiCatBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.setup_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.softEntrance(),
                    text = stringResource(R.string.setup_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(28.dp))

                PillowCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .softEntrance(delayMillis = 80),
                    contentPadding = 20.dp,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.setup_label_name),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { if (it.length <= 24) name = it },
                            placeholder = { Text(stringResource(R.string.setup_placeholder_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary,
                            ),
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = stringResource(R.string.setup_label_cat_name),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = catName,
                            onValueChange = { if (it.length <= 24) catName = it },
                            placeholder = { Text(stringResource(R.string.setup_placeholder_cat_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .softEntrance(delayMillis = 200),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = stringResource(R.string.setup_label_goals),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    SetupGoalItem(
                        title = stringResource(R.string.goal_steps_title),
                        value = stepGoal,
                        unit = stringResource(R.string.unit_steps),
                        step = 1000,
                        color = PremiumPink,
                        onValueChange = { stepGoal = it },
                        maxValue = 30000,
                    )

                    SetupGoalItem(
                        title = stringResource(R.string.goal_water_title),
                        value = waterGoal,
                        unit = stringResource(R.string.unit_ml),
                        step = 250,
                        color = PremiumBlue,
                        onValueChange = { waterGoal = it },
                        maxValue = 5000,
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                ActionPillButton(
                    text = stringResource(R.string.setup_btn_start),
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = context.getString(R.string.setup_error_name)
                        } else if (catName.isBlank()) {
                            errorMessage = context.getString(R.string.setup_error_cat_name)
                        } else {
                            viewModel.saveUserProfile(name, catName, stepGoal, waterGoal)
                            markNewUser()
                            onSetupComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .softEntrance(delayMillis = 280),
                    leadingEmoji = "🚀",
                )

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
    onValueChange: (Int) -> Unit,
    maxValue: Int = Int.MAX_VALUE,
) {
    PillowCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 16.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$value",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelLarge,
                        color = color.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }

            StepperButton(
                icon = Icons.Default.Remove,
                color = color,
                contentDescription = stringResource(R.string.btn_decrease),
                onClick = { if (value > step) onValueChange(value - step) },
            )
            Spacer(modifier = Modifier.width(12.dp))
            StepperButton(
                icon = Icons.Default.Add,
                color = color,
                contentDescription = stringResource(R.string.btn_increase),
                onClick = { if (value + step <= maxValue) onValueChange(value + step) },
            )
        }
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .pillowPress(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = color)
    }
}
