package dev.gumil.acorn

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.ui.tooling.preview.Preview
import dev.gumil.acorn.ui.AcornComposeTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AcornComposeTheme {
                // A surface container using the 'background' color from the theme
                val screen = remember { mutableStateOf<Pair<Screen, Int>>(Screen.One to 5000) }
                Screen(screen.value) { screen.value = it }
            }
        }
    }
}

@Composable
fun Screen(
    screen: Pair<Screen, Int>,
    listener: (Pair<Screen, Int>) -> Unit
) {
    Log.d("tantrums", "animation = ${screen.second}")
    Crossfade(
        screen.first,
        animation = tween(screen.second)
    ) { currentScreen ->
        when (currentScreen) {
            is Screen.One -> ScreenOne { listener(Screen.Two to 2000) }
            is Screen.Two -> ScreenTwo { listener(Screen.One to 1000) }
        }
    }
}

sealed class Screen {
    object One : Screen()
    object Two : Screen()
}

@Composable
fun ScreenOne(
    onClicked: () -> Unit
) {
    Box(
        modifier = Modifier.clickable(onClick = { onClicked() }) then Modifier.fillMaxSize()
    ) {
        key("test") {
            Greeting("Android")
        }
    }
}

@Composable
fun ScreenTwo(
    onClicked: () -> Unit
) {
    Box(
        gravity = Alignment.Center,
        modifier = Modifier.clickable(onClick = { onClicked() }) then Modifier.fillMaxSize()
    ) {
        key("test") {
            Text(
                text = "HELLO!!!!!!!!!!!!"
            )
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ScreenTwo{}
}