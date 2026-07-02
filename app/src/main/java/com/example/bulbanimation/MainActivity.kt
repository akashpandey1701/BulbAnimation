package com.example.bulbanimation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.bulbanimation.ui.theme.BulbAnimationTheme
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BulbAnimationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   Screen(modifier=Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun Screen(modifier: Modifier) {
    var isOn by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        targetValue = if (isOn)
           Color (0xFF202842)
        else
            Color(0xFF222326),
        animationSpec = tween(500)
    )

    Box(
        modifier = Modifier.fillMaxSize().background(color = backgroundColor),
        contentAlignment = Alignment.TopCenter
    ) {


        val offsetY = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()
        val maxPull = 200f

        val glowAlpha by animateFloatAsState(
            targetValue = if (isOn) 1f else 0f,
            animationSpec = tween(300),
        )

        val bulbColor by animateColorAsState(
            targetValue = if (isOn) Color(0xFFFFF59D) else Color.White,
            animationSpec = tween(300),
        )

        val rayAlpha by animateFloatAsState(
            targetValue = if (isOn) 1f else 0.25f,
            animationSpec = tween(300),
        )

        Canvas(
            modifier = Modifier
                .size(300.dp, 500.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val newValue = (offsetY.value + dragAmount.y).coerceIn(0f, maxPull)
                                offsetY.snapTo(newValue)
                            }
                        },
                        onDragEnd = {
                            isOn = !isOn
                            scope.launch {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetY.animateTo(0f, spring()) }
                        }
                    )
                }
        ) {
            val centre = size.width / 2
            val baseWireEnd = size.height / 2
            val wireEnd = baseWireEnd + offsetY.value

            drawLine(
                start = Offset(centre, 0f),
                end = Offset(centre, wireEnd),
                color = Color.Black,
                strokeWidth = 10f
            )

            val capWidth = 120f
            val capHeight = 70f
            val neckWidth = 120f
            val neckHeight = 90f
            val capBottom = wireEnd + capHeight
            val gap = 15f
            val neckTop = capBottom + gap

            val bulbPath = Path().apply {
                moveTo(centre - neckWidth / 2, neckTop + neckHeight)
                quadraticBezierTo(centre - 320f, neckTop + 260f, centre - 220f, neckTop + 580f)
                cubicTo(centre - 180f, neckTop + 820f, centre + 180f, neckTop + 820f, centre + 220f, neckTop + 580f)
                quadraticBezierTo(centre + 320f, neckTop + 260f, centre + neckWidth / 2, neckTop + neckHeight)
                close()
            }

            val bulbCenter = Offset(centre, neckTop + 420f)

            if (glowAlpha > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFF59D).copy(alpha = glowAlpha * 0.8f),
                            Color.Transparent
                        ),
                        center = bulbCenter,
                        radius = 260f
                    ),
                    center = bulbCenter,
                    radius = 260f
                )
            }
            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(x = centre - capWidth / 2, y = wireEnd),
                size = Size(width = capWidth, height = capHeight),
                cornerRadius = CornerRadius(12f, 12f)
            )

            drawPath(path = bulbPath, color = bulbColor)

            drawRect(
                color = bulbColor,
                topLeft = Offset(x = centre - neckWidth / 2, y = neckTop),
                size = Size(width = neckWidth, height = neckHeight)
            )

            if(isOn)
            {
            val rayCount = 8
            val innerRadius = 320f
            val rayLength = 95f
            val angleStep = 360.0 / rayCount

            repeat(rayCount) { i ->
                val angle = Math.toRadians(angleStep * i)
                val start = Offset(
                    bulbCenter.x + cos(angle).toFloat() * innerRadius,
                    bulbCenter.y + sin(angle).toFloat() * innerRadius
                )
                val end = Offset(
                    bulbCenter.x + cos(angle).toFloat() * (innerRadius + rayLength),
                    bulbCenter.y + sin(angle).toFloat() * (innerRadius + rayLength)
                )
                drawLine(
                    color = bulbColor,
                    start = start,
                    end = end,
                    strokeWidth = 8f + (rayAlpha * 4f),
                    cap = StrokeCap.Round
                )
            }
            }
        }
        Text(
            text = "Pull the cord down to toggle the light",
            color = Color.White,
            modifier = Modifier
                .padding(top = 680.dp)
        )
    }
}