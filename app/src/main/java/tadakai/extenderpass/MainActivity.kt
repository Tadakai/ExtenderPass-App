package tadakai.extenderpass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import tadakai.extenderpass.ui.MainViewModel
import tadakai.extenderpass.ui.screens.MainScreen
import tadakai.extenderpass.ui.theme.ExtenderPassTheme

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Let the Compose layout draw behind the system bars for an edge-to-edge look
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ExtenderPassTheme {
                MainScreen(vm = vm)
            }
        }
    }
}
