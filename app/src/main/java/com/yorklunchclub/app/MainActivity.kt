package com.yorklunchclub.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.yorklunchclub.app.ui.navigation.AppNavGraph
import com.yorklunchclub.app.ui.theme.YorkLunchClubTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YorkLunchClubTheme {
                AppNavGraph()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppRootPreview() {
    YorkLunchClubTheme {
        AppNavGraph()
    }
}
