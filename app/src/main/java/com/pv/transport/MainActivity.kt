package com.pv.transport

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.pv.transport.auth.AuthPrefs
import com.pv.transport.network.WebSocketManager
import com.pv.transport.presentation.AppNavigation
import com.pv.transport.repository.MasterDataRepository
import com.pv.transport.ui.theme.PVTransportTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authPrefs: AuthPrefs
    @Inject lateinit var wsManager: WebSocketManager
    @Inject lateinit var masterDataRepository: MasterDataRepository

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* optional */ }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { false }

        // Set locale based on preference
        val language = authPrefs.getLanguage() ?: "en"
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Only on a real cold start, so a rotation does not trigger another sync.
        if (savedInstanceState == null && authPrefs.isLoggedIn()) {
            lifecycleScope.launch { masterDataRepository.syncInitialData() }
        }

        // Android 13+: sync-completed notifications need this at runtime
        if (savedInstanceState == null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        enableEdgeToEdge()
        setContent {
            PVTransportTheme {
                AppNavigation()
            }
        }
    }

    /**
     * Catches the app that is left open for days: the repository only goes to the network once
     * the cached master data is old enough, so a normal resume costs nothing.
     */
    override fun onResume() {
        super.onResume()
        if (authPrefs.isLoggedIn()) {
            lifecycleScope.launch { masterDataRepository.refreshIfStale() }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PVTransportTheme {
        Greeting("Android")
    }
}