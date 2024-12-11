package org.tracker.sdk
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.tracker.sdk.presenter.MainViewModel
import org.tracker.sdk.ui.screentwo.ScreenTwo
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
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "screen_one") {
                    composable("screen_one") {
                        AnalyticsExampleScreen(analyticsManager, viewModel, navController)
                    }
                    composable("screen_two") {
                        ScreenTwo(context = LocalContext.current, navController = navController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsExampleScreen(
    analyticsManager: AnalyticsManager,
    viewModel: MainViewModel,
    navController: NavHostController,
) {
    val logMessage by viewModel.logMessage.collectAsState()
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Screen One", fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.text_color),
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = colorResource(id = R.color.purple_500)
                )
            )
        },
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            LaunchedEffect(Unit) {
                viewModel.toastEvent.collect { message ->
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),

                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = logMessage,
                        fontSize = 16.sp,
                        color = colorResource(R.color.card_text_color),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = {
                        analyticsManager.startSession(UUID.randomUUID().toString()) { result ->
                            when (result) {
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
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple_200),)
                ) {
                    Text("Start Session", fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_color)
                        )
                }

                Button(
                    onClick = {
                         analyticsManager.startScreenTracking("MainActivity") { result->
                             when (result) {
                                 is Result.Success -> {
                                     viewModel.updateLogMessage(result.data!!)
                                 }
                                 else -> {
                                     viewModel.triggerToastMessage(result.data!!)
                                 }
                             }
                         }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.end_color),)
                ) {
                    Text("Start Screen Timer Event", fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_color)
                    )
                }

                Button(
                    onClick = {
                        val sampleEvent = AnalyticsEvent(
                            eventName = "ButtonClicked",
                            properties = mutableMapOf(
                                "screen" to "MainActivity",
                                "action" to "TrackEvent"
                            )
                        )
                        analyticsManager.trackEvent(sampleEvent) { result ->
                            when (result) {
                                is Result.Success -> {
                                    viewModel.updateLogMessage("Event 'ButtonClicked' tracked!")
                                }
                                else -> {
                                    viewModel.triggerToastMessage(result.data!!)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.purple_700))
                ) {
                    Text("Start  Button Clicked Event", fontWeight = FontWeight.Bold, color = colorResource(R.color.text_color)
                        )
                }
                Button(
                    onClick = {
                        if (analyticsManager.getCurrentSession()!=null) {
                            analyticsManager.endScreenTracking("MainActivity") {
                                when (it) {
                                    is Result.Success ->{
                                        Log.d("MainActivity", it.data.toString())
                                    }
                                    is Result.Error -> {
                                        Log.e("MainActivity", it.data.toString())
                                    }
                                }
                                navController.navigate("screen_two")
                            }
                        } else {
                            viewModel.triggerToastMessage("No active Session Found start a active session first")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.teal_700))
                ) {
                    Text("Results", fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_color)
                        )
                }

                Button(
                    onClick = {
                        analyticsManager.endSession() { result ->
                            when (result) {
                                is Result.Success -> {
                                    viewModel.updateLogMessage("Session Ended Successfully!")
                                }
                                else -> {
                                    viewModel.triggerToastMessage(result.data!!)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.end_color),)
                ) {
                    Text("End Session", fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_color)
                        )
                }
            }
        }
    )
}
