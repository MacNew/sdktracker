package org.tracker.sdk.ui.screentwo

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.tracker.sdk.R
import org.tracker.trackersdk.AnalyticsManager
import org.tracker.trackersdk.data.model.AnalyticsEvent

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTwo(context: Context, navController: NavHostController) {
    val events = remember { mutableStateListOf<AnalyticsEvent>() }

    LaunchedEffect(Unit) {
        val analyticsManager = AnalyticsManager.getInstance(context)
        events.clear()
        events.addAll(analyticsManager.getEvents())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics Results",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colorResource(id = R.color.text_color)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.text_color)
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = colorResource(id = R.color.purple_500)
                )
            )
        },
        content = {
                innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (events.isEmpty()) {
                    Text(
                        text = "No events tracked yet.",
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.text_color),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        items(events) { event ->
                            EventCard(event = event)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun EventCard(event: AnalyticsEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_container_color))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Event: ${event.eventName}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colorResource(id = R.color.purple_500)
            )
            Spacer(modifier = Modifier.height(8.dp))
            event.properties.forEach { (key, value) ->
                Text(
                    text = "$key: $value",
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.text_color)
                )
            }
        }
    }
}