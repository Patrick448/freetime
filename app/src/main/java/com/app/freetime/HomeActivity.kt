package com.app.freetime

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.app.freetime.Model.Tip
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var dh: DataHandler = DataHandler()

        /*val db = Firebase.firestore
        var dataSets = listOf<Tip>()
        db.collection("tips").get()
            .addOnFailureListener{
                info ->
                Toast.makeText(
                    baseContext,
                    info.toString(),
                    Toast.LENGTH_SHORT,
                ).show()
                Log.d("fb-test", info.toString())
            }
            .addOnSuccessListener { documents ->
                Log.d("test-fb", "Test log")
                for (document in documents) {
                    val title = document["title"].toString()
                    val text = document["text"].toString()
                    val isFavorite: Boolean = document["favorite"] as Boolean
                    val tip :Tip = Tip(id = "", title=title, text = text, isFavorite = isFavorite)
                    Log.d("test-fb", tip.toString())
                    print(tip.toString())
                }
        }*/




        setContent {
            FreetimeTheme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimerView(
                        modifier = Modifier.padding(innerPadding),
                        onClickStartStop = {},
                        onClickReset = {},
                        onClickTips = ::goToTips,
                        onClickLogout = ::logOut,
                        timeSeconds = 0,
                        sessionState = SessionState.WORK
                    )
                }
            }
        }
    }

    private fun logOut(){
        Firebase.auth.signOut()
        finish()
        Toast.makeText(
            baseContext,
            "Logout",
            Toast.LENGTH_SHORT,
        ).show()
    }

    private fun goToTips() {
        val intent = Intent(this, TaskActivity::class.java)
        startActivity(intent)
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
        //text in a box with rounded corners

        Row(){
            IconButton(
                onClick = {onClickTips()},
                modifier = Modifier.weight(1f)

            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tips"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f),
                horizontalArrangement = Arrangement.Center,
            ){
                Box(
                    modifier = Modifier
                        .background(
                            Color.LightGray,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(BorderStroke(2.dp, Color.Black), shape = RoundedCornerShape(20.dp))
                        .padding(8.dp)


                ) {
                    Text(
                        text = getSessionStateString(sessionState),
                        modifier = Modifier,
                        color = Color.Black,
                        fontWeight = Bold
                    )
                }

            }


            IconButton(
                onClick = {onClickLogout()},
                modifier = Modifier.weight(1f)

            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Logout"
                )
            }


        }


        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeString,
                    modifier = Modifier.padding(0.dp, 100.dp),
                    style = TextStyle(
                        fontSize = 120.sp,
                        fontWeight = FontWeight.ExtraBold,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                )

            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .background(Color.LightGray, shape = RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More"
                    )
                }
                IconButton(
                    onClick = onClickStartStop,
                    modifier = Modifier
                        .background(Color.LightGray, shape = RoundedCornerShape(16.dp))
                        .size(75.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Pause Icon"
                    )
                }
                IconButton(
                    onClick = onClickReset,
                    modifier = Modifier
                        .background(Color.LightGray, shape = RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Pause Icon"
                    )
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
            timeSeconds = 200,
            sessionState = SessionState.WORK
        )
    }
}