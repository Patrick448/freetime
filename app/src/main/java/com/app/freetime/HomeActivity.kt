package com.app.freetime

import android.os.Bundle
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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import com.app.freetime.Model.Tip
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Firebase.firestore
        var dataSets = listOf<Tip>()
        db.collection("tips").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val title = document["title"].toString()
                val text = document["text"].toString()
                val isFavorite: Boolean = document["favorite"] as Boolean
                val tip :Tip = Tip(id = "", title=title, text = text, isFavorite = isFavorite)
                print(tip.toString())
            }
            //call a function to work with your array
           // myFun(dataSets)
        }


        setContent {
            FreetimeTheme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimerView(
                        modifier = Modifier.padding(innerPadding),
                        onClickStartStop = {},
                        onClickReset = {},
                        timeSeconds = 0,
                        sessionState = SessionState.WORK
                    )
                }
            }
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
            timeSeconds = 200,
            sessionState = SessionState.WORK
        )
    }
}