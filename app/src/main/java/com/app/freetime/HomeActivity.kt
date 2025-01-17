package com.app.freetime

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.freetime.ui.theme.FreetimeTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.app.freetime.Model.Tip
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.nio.file.WatchEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import android.os.IBinder
import androidx.activity.OnBackPressedCallback
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay


class HomeActivity : ComponentActivity() {


    private var timerService: TimerService? = null
    private var timerRunning by mutableStateOf(false)
    private var timerPaused by mutableStateOf(false)

    private var serviceBoundState by mutableStateOf(false)
    private var time by mutableStateOf(5)
    private var sessionState by mutableStateOf(SessionState.WORK)
    private var backPressedOnce = false


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.LocalBinder
            timerService = binder.getService()
            timerService?.onFinishSession = {}

            val combinedFlow = combine(
                timerService?.timeFlow ?: flowOf(),
                timerService?.sessionStatusFlow ?: flowOf(),
                timerService?.timerRunningFlow ?: flowOf()
            ) { time, sessionStatus, timerRunning ->
                Triple(time, sessionStatus, timerRunning)
            }

            lifecycleScope.launch {
                combinedFlow.collect { (newTime, newSessionStatus, newTimerRunning) ->
                    time = newTime
                    sessionState = newSessionStatus
                    timerRunning = newTimerRunning
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceBoundState = false
            timerService = null
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestNotificationPermission()
        createSharedPrefs()
        tryToBindToServiceIfRunning()


        var dh: DataHandler = DataHandler()

        setContent {
            FreetimeTheme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimerView(
                        modifier = Modifier.padding(innerPadding),
                        onClickStartStop = ::startStopService,
                        onClickReset = ::resetAll,
                        onClickTips = ::goToTips,
                        onClickTasks = ::goToTasks,
                        onClickLogout = ::logOut,
                        timeSeconds = time,
                        sessionState = sessionState
                    )
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedOnce) {
                    finish()
                } else {
                    backPressedOnce = true
                    Toast.makeText(this@HomeActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()

                    lifecycleScope.launch {
                        delay(2000L)
                        backPressedOnce = false
                    }
                }
            }
        })
    }



    private fun logOut(){
        Firebase.auth.signOut()
        val intent = Intent(this, LoginRegisterActivity::class.java)
        startActivity(intent)
        Toast.makeText(
            baseContext,
            "Logout",
            Toast.LENGTH_SHORT,
        ).show()
        finish()
    }

    private fun goToTips() {
        val intent = Intent(this, TipsActivity::class.java)
        startActivity(intent)
    }

    private fun goToTasks() {
        val intent = Intent(this, TaskActivity::class.java)
        startActivity(intent)
    }

    private fun checkAndRequestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )) {
                android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                }

                else -> {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (timerRunning) {
            unbindService(connection)
        }
    }

    private fun tryToBindToServiceIfRunning() {
        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }



    private fun runService() {
        val intent = Intent(this, TimerService::class.java)

        //intent.putExtra("time", time)
        // intent.putExtra("sessionState", sessionState)
        startForegroundService(intent)
        tryToBindToServiceIfRunning()
        timerRunning = true
        timerPaused = false
    }

    private fun resumeService(){
        val intent = Intent(this, TimerService::class.java)
        intent.putExtra("action", "resume")
        startForegroundService(intent)
        tryToBindToServiceIfRunning()
        timerRunning = true
        timerPaused = false
    }

    private fun stopService() {
        timerService?.stopForegroundService()
        unbindService(connection)
        timerRunning = false
    }

    private fun resetAll() {
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("current_session")
        editor.remove("remaining_time")
        editor.remove("current_state_index")
        editor.apply()
        if (timerRunning) stopService()
        //if (!restoreSessionStateSharedPrefs()) {
        //   Toast.makeText(this, "Reset", Toast.LENGTH_SHORT).show()
        // }
        time = sharedPref.getInt("focusTime", 25) * 60
        sessionState = SessionState.WORK
    }

    private fun startStopService() {
        if (timerRunning) {
            stopService()
            timerPaused = true
        } else if(timerPaused) {
            resumeService()
        }else{
            runService()
            timerPaused = false
        }
    }

    private fun createSharedPrefs() {
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("shortBreak", 5)
        editor.putInt("longBreak", 15)
        editor.putInt("focusTime", 1)
        editor.apply()
    }

    private fun restoreSessionStateSharedPrefs(): Boolean {
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val storedCurrentSession = sharedPref.getInt("current_session", -1)
        val storedTime = sharedPref.getInt("remaining_time", -1)

        return if (storedCurrentSession != -1 && storedTime != -1) {
            time = storedTime
            sessionState = SessionState.entries[storedCurrentSession]
            true
        } else {
            time = sharedPref.getInt("focusTime", 25)
            sessionState = SessionState.WORK
            false
        }
    }


}


enum class SessionState {
    WORK,
    SHORT_BREAK,
    LONG_BREAK,
    WAITING_FOR_USER_INPUT;

    companion object {
        @Composable
        fun getSessionStateString(sessionState: Enum<SessionState>): String {
            return when (sessionState) {
                WORK -> "Focus"
                SHORT_BREAK -> "Short Break"
                LONG_BREAK -> "Long Break"
                else -> {
                    ShowUnknownStateError()
                    "Unknown"
                }
            }
        }

        @Composable
        private fun ShowUnknownStateError() {
            //ErrorSnackBar(mainMessage = "Error", subMessage = "Unknown session state encountered")
        }
    }
}

fun getMinsSecs(time: Int): String {
    val mins = time / 60
    val secs = time % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

fun getSessionStateString(sessionState: Enum<SessionState>): String {
    return when (sessionState) {
        SessionState.WORK -> "Focus"
        SessionState.SHORT_BREAK -> "Short Break"
        SessionState.LONG_BREAK -> "Long Break"
        else -> "Unknown"
    }
}


@Composable
fun TimerView(
    modifier: Modifier = Modifier,
    onClickStartStop: () -> Unit,
    onClickReset: () -> Unit,
    onClickLogout: () -> Unit,
    onClickTips: () -> Unit,
    onClickTasks:() -> Unit,
    timeSeconds: Int,
    sessionState: Enum<SessionState>
) {
    val timeString = getMinsSecs(timeSeconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 50.dp, 0.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left section (aligned to the left)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { onClickTasks() }) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Tips")
                }
                IconButton(onClick = { onClickTips() }) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite")
                }
            }

            // Center section (aligned to the center)
            Row(
                modifier = Modifier.weight(2f),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.LightGray, shape = RoundedCornerShape(20.dp))
                        .border(BorderStroke(2.dp, Color.Black), shape = RoundedCornerShape(20.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = getSessionStateString(sessionState),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right section (aligned to the right)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onClickLogout() }) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout")
                }
            }
        }

        // Timer display
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeString,
                modifier = Modifier.padding(0.dp, 100.dp),
                style = TextStyle(
                    fontSize = 120.sp,
                    fontWeight = FontWeight.ExtraBold,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {},
                    modifier = Modifier.background(Color.LightGray, shape = RoundedCornerShape(16.dp))
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                }
                IconButton(
                    onClick = onClickStartStop,
                    modifier = Modifier
                        .background(Color.LightGray, shape = RoundedCornerShape(16.dp))
                        .size(75.dp)
                ) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Start/Stop")
                }
                IconButton(
                    onClick = onClickReset,
                    modifier = Modifier.background(Color.LightGray, shape = RoundedCornerShape(16.dp))
                ) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Reset")
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun TimerPreview() {
    FreetimeTheme() {
        TimerView(
            onClickStartStop = {},
            onClickReset = {},
            onClickLogout = {},
            onClickTips = {},
            onClickTasks = {},
            timeSeconds = 200,
            sessionState = SessionState.WORK
        )
    }
}