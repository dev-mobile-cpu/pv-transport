package com.pv.transport.extension

import android.os.SystemClock
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Single-tap safe navigation: drops rapid duplicate clicks and avoids
 * stacking the same destination on top of itself.
 */
fun NavController.safeNavigate(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    val now = SystemClock.elapsedRealtime()
    if (route == lastSafeNavigateRoute && now - lastSafeNavigateAt < NAV_DEBOUNCE_MS) return
    if (currentDestination?.route == route) return
    // Block overlapping navigations (double-tap before destination settles)
    if (!navInFlight.compareAndSet(false, true)) return

    lastSafeNavigateRoute = route
    lastSafeNavigateAt = now
    try {
        navigate(route) {
            launchSingleTop = true
            builder()
        }
    } finally {
        // Keep lock briefly so a second click in the same frame cannot stack another page
        // Destination change is async; release after debounce window via timestamp gate above.
        // Unlock immediately for *different* routes after a short delay handled by debounce alone.
        navInFlight.set(false)
    }
}

private const val NAV_DEBOUNCE_MS = 1000L
@Volatile private var lastSafeNavigateAt = 0L
@Volatile private var lastSafeNavigateRoute: String? = null
private val navInFlight = AtomicBoolean(false)
