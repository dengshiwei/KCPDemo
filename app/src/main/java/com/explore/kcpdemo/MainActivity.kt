package com.explore.kcpdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.explore.kcpdemo.TestTarget.nestedFunction
import com.explore.kcpdemo.TestTarget.simpleFunction
import com.explore.kcpdemo.TestTarget.sumFunction
import com.explore.kcpdemo.ui.theme.KCPDemoTheme
import kotlin.text.Typography.prime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KCPDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding).clickable {
                            greet("clickable")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

fun greet(name: String) {
    println("=== TestTarget main start ===")
    simpleFunction()
    sumFunction(2, 3)
    nestedFunction()
    println("=== TestTarget main end ===")
}