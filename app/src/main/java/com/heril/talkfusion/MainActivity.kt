package com.heril.talkfusion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.FirebaseApp
import com.heril.talkfusion.ui.Navigation
import com.heril.talkfusion.ui.theme.TalkFusionTheme
import com.heril.talkfusion.ui.viewmodels.FirebaseViewModel
import com.heril.talkfusion.utils.FirebaseViewModelProvider

//02:93:C4:83:40:07:1B:A6:1B:65:B4:CD:B3:D1:07:49:C1:2D:8E:25
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.auto(
//                Color.Transparent.toArgb(),Color.Transparent.toArgb()
//            ),
//            navigationBarStyle = SystemBarStyle.auto(
//                Color.Transparent.toArgb(),Color.Transparent.toArgb()
//            )
//        )
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
//        val firebaseViewModel = FirebaseViewModel()
        val firebaseViewModel = FirebaseViewModelProvider.instance
        setContent {
            TalkFusionTheme {
                Surface(modifier=Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Navigation(firebaseViewModel)
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun Preview() {
//    Navigation()
//}