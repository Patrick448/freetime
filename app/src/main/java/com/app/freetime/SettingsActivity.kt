package com.app.freetime

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.app.freetime.ui.theme.FreetimeTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import android.content.Context
import android.content.SharedPreferences

class SettingsActivity : ComponentActivity() {

    val dataHandler = DataHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load saved settings from SharedPreferences
        val longBreakDuration = loadSetting(this, "longBreak", 15) // default value 15
        val shortBreakDuration = loadSetting(this, "shortBreak", 5)  // default value 5
        val focusTimeDuration = loadSetting(this, "focusTime", 25)    // default value 25

        setContent {
            FreetimeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        onChangeSettings = ::onChangeSetting,
                        onLogoutClick = ::logOut,
                        settings = mapOf(
                            "longBreak" to longBreakDuration,
                            "shortBreak" to shortBreakDuration,
                            "focusTime" to focusTimeDuration
                        )
                    )
                }
            }
        }
    }

    // Function to handle changes in settings and save them to SharedPreferences
    fun onChangeSetting(settingName: String, value: Int) {
        saveSetting(this, settingName, value)
        var pref = Model.Preferences(
            getSharedPreferences(this).getInt("shortBreak", 0),
            getSharedPreferences(this).getInt("longBreak", 0),
            getSharedPreferences(this).getInt("focusTime", 0))

        dataHandler.updateOrCreatePreferences(
            pref,
            { Toast.makeText(this, "Synced settings", Toast.LENGTH_SHORT).show()},
            { Toast.makeText(this, "Error syncing settings", Toast.LENGTH_SHORT).show()}

        )

    }

    private fun logOut() {
        Firebase.auth.signOut()
        val intent = Intent(this, LoginRegisterActivity::class.java)
        startActivity(intent)
        Toast.makeText(baseContext, "Logout", Toast.LENGTH_SHORT).show()
        finishAffinity()
    }
}



// Function to get SharedPreferences instance
private fun getSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
}

// Function to save settings (long break, short break, focus time)
private fun saveSetting(context: Context, settingName: String, value: Int) {
    val sharedPreferences = getSharedPreferences(context)
    val editor = sharedPreferences.edit()
    editor.putInt(settingName, value)
    editor.apply()
}

// Function to load settings (long break, short break, focus time)
private fun loadSetting(context: Context, settingName: String, defaultValue: Int): Int {
    val sharedPreferences = getSharedPreferences(context)
    return sharedPreferences.getInt(settingName, defaultValue)
}



@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settings: Map<String, Int>,  // Receiving the initial settings
    onChangeSettings: (String, Int) -> Unit,
    onLogoutClick: () -> Unit,
) {
    var longBreak by remember { mutableStateOf(settings["longBreak"] ?: 15) }
    var shortBreak by remember { mutableStateOf(settings["shortBreak"] ?: 5) }
    var focusTime by remember { mutableStateOf(settings["focusTime"] ?: 25) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Settings",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )

        DurationSlider(
            label = "Focus Time",
            duration = focusTime.toFloat(),
            onValueChange = { focusTime = it.toInt();  },
            onValueChangeFinished = {onChangeSettings("focusTime", focusTime)}
        )

        DurationSlider(
            label = "Short Break",
            duration = shortBreak.toFloat(),
            onValueChange = { shortBreak = it.toInt();  },
            onValueChangeFinished = {onChangeSettings("shortBreak", shortBreak)}
        )

        DurationSlider(
            label = "Long Break",
            duration = longBreak.toFloat(),
            onValueChange = { longBreak = it.toInt();  },
            onValueChangeFinished = {onChangeSettings("longBreak", longBreak)}
        )

        Divider(modifier = Modifier.padding(vertical = 2.dp))

        ListItemIconButton(
            text = "Logout",
            icon = Icons.Filled.ExitToApp,
            onClick = { onLogoutClick() }
        )
    }
}

@Composable
fun ListItemIconButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun DurationSlider(label: String, duration: Float, onValueChange: (Float) -> Unit, onValueChangeFinished: () ->Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$label: ${duration.toInt()} min")
        Slider(
            value = duration,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = 1f..60f, // Value range from 1 to 60 minutes
            steps = 59, // Steps to allow adjusting by 1 minute
            modifier = Modifier.fillMaxWidth()
        )
    }
}

