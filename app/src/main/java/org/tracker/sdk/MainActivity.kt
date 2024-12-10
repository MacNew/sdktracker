package org.tracker.sdk
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tracker.sdk.presenter.MainViewModel
import org.tracker.sdk.ui.theme.TrackersdkTheme
import org.tracker.trackersdk.AnalyticsManager
import org.tracker.trackersdk.data.Result
import org.tracker.trackersdk.data.model.AnalyticsEvent
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val analyticsManager = AnalyticsManager.getInstance(this)
        setContent {
            TrackersdkTheme {
                AnalyticsExampleScreen(analyticsManager, viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsExampleScreen(analyticsManager: AnalyticsManager, viewModel: MainViewModel) {
    val logMessage by viewModel.logMessage.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics SDK Demo", fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text_color))
                 },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = colorResource(id = R.color.purple_500) // Use colorResource here
                )
            )
        },
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            LaunchedEffect(Unit) {
                viewModel.toastEvent.collect { message->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = logMessage,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // Start Session Button
                Button(
                    onClick = {
                        analyticsManager.startSession(UUID.randomUUID().toString()) { result->
                            when(result) {
                                is Result.Success -> {
                                    viewModel.updateLogMessage("Session Started Successfully!")
                                }
                                else -> {
                                   viewModel.updateLogMessage("Unable to Start Session")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC6))
                ) {
                    Text("Start Session", fontWeight = FontWeight.Bold)
                }

                // Track Event Button
                Button(
                    onClick = {
                        val sampleEvent = AnalyticsEvent(
                            eventName = "ButtonClicked",
                            properties = mutableMapOf(
                                "screen" to "MainActivity",
                                "action" to "TrackEvent"
                            )
                        )
                        analyticsManager.trackEvent(sampleEvent) { result->
                            when(result) {
                                is Result.Success -> {
                                    viewModel.updateLogMessage("Event 'ButtonClicked' tracked!")
                                } else -> {
                                    viewModel.triggerToastMessage(result.data!!)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                ) {
                    Text("Track Event", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        analyticsManager.endSession() { result ->
                            when(result) {
                                is Result.Success -> {
                                    viewModel.updateLogMessage("Session Ended Successfully!")
                                } else-> {
                                    viewModel.triggerToastMessage(result.data!!)
                                }
                            }
                        }
                       // viewModel.updateLogMessage("Session ended. Events persisted.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Text("End Session", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}
