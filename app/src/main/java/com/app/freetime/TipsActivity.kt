package com.app.freetime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.freetime.Model.Tip
import com.app.freetime.ui.theme.FreetimeTheme
import kotlinx.coroutines.launch

class TipsActivity : ComponentActivity() {

    var dataHandler = DataHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FreetimeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val tipsList = remember { mutableStateOf(emptyList<Tip>()) }
                    val scope = rememberCoroutineScope()
                    val dataHandler = DataHandler()
                    val isLoading = remember { mutableStateOf(true) }

                    // Fetch tips when the UI is first composed
                    LaunchedEffect(Unit) {
                        scope.launch {
                            dataHandler.getAllTips { tips ->
                                tipsList.value = tips
                                isLoading.value = false
                            }

                        }

                    }

                    Column(modifier = Modifier.padding(innerPadding)) {
                        Text(
                            text = "Tips",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        if(isLoading.value){
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }


                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tipsList.value) { tip ->
                                TipItem(tip, ::toggleLikeTip)
                            }
                        }
                    }
                }
            }
        }
    }


    private fun toggleLikeTip(tip: Tip){
        var newTip = tip.copy()
        newTip.favorite = !tip.favorite
        dataHandler.updateTip(
            tip.id,
            newTip,
            {println("Successfully updated favorite status for ${tip.id}")},
            {e -> println("Error updating favorite status: ${e.message}")})
    }
}



@Composable
fun TipItem(tip: Tip, onLikeToggle: (Tip) -> Unit) {
    var isLiked by remember { mutableStateOf(tip.favorite) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

//            IconButton(
//                onClick = {
//                    isLiked = !isLiked
//                    onLikeToggle(tip) // Update Firebase if needed
//                }
//            ) {
//                Icon(
//                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
//                    contentDescription = "Like",
//                    tint = if (isLiked) Color.Black else Color.Gray
//                )
//            }
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