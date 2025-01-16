package com.app.freetime

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.app.freetime.Model.Tip
import com.app.freetime.ui.theme.FreetimeTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext


class TipsActivity : ComponentActivity() {
    var tipsList: MutableState<List<Tip>> = mutableStateOf(emptyList())



    override fun onCreate(savedInstanceState: Bundle?) = runBlocking{
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //var tipsList : List<Tip> = emptyList()
        var dh = DataHandler()

        launch{
            dh.getAllTips{ tips -> tipsList.value = tips}
        }


        setContent {

            FreetimeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        Text(
                            text = "xxxxx",
                            modifier = Modifier,
                            color = Color.Black,
                            fontWeight = Bold
                        )
                        tipsList.value.forEach() { tip ->
                            Text(
                                text = tip.title,
                                modifier = Modifier,
                                color = Color.Black,
                                fontWeight = Bold
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    FreetimeTheme {
        Greeting2("Android")
    }
}