package com.example.cs501_hw4_q1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.example.cs501_hw4_q1.ui.theme.Cs501_hw4_q1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cs501_hw4_q1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BallGame(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BallGame(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val gyro = remember { sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }

    var size by remember { mutableStateOf(IntSize.Zero) }
    var x by remember { mutableStateOf(80f) }
    var y by remember { mutableStateOf(80f) }

    val r = 28f

    fun walls() = listOf(
        Rect(120f, 0f, 150f, 500f),
        Rect(250f, 220f, 280f, 790f),
        Rect(380f, 0f, 410f, 650f),
        Rect(500f, 350f, 530f, 790f),
        Rect(0f, 620f, 250f, 650f),
        Rect(250f, 760f, 530f, 790f)
    )

    val goal = Rect(560f, 100f, 700f, 240f)

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (size.width == 0) return

                val oldX = x
                val oldY = y

                x += event.values[1] * 18f
                y += event.values[0] * 18f

                x = x.coerceIn(r, size.width - r)
                y = y.coerceIn(r, size.height - r)

                val ball = Rect(x - r, y - r, x + r, y + r)
                if (walls().any { it.overlaps(ball) }) {
                    x = oldX
                    y = oldY
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (gyro != null) {
            sensorManager.registerListener(listener, gyro, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val won = remember(x, y) {
        goal.overlaps(Rect(x - r, y - r, x + r, y + r))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
    ) {
        Canvas(modifier = modifier.fillMaxSize()) {
            drawRect(Color(0xFFF5F5F5))

            drawRect(
                color = Color.Green,
                topLeft = Offset(goal.left, goal.top),
                size = androidx.compose.ui.geometry.Size(goal.width, goal.height)
            )

            walls().forEach {
                drawRect(
                    color = Color.DarkGray,
                    topLeft = Offset(it.left, it.top),
                    size = androidx.compose.ui.geometry.Size(it.width, it.height)
                )
            }

            drawCircle(
                color = Color.Red,
                radius = r,
                center = Offset(x, y)
            )
        }

        Text(
            text = if (won) "You Win!" else "Tilt to move",
            modifier = modifier.align(Alignment.TopCenter)
        )
    }
}