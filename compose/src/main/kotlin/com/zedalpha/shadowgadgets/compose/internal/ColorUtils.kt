package com.zedalpha.shadowgadgets.compose.internal

import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import com.zedalpha.shadowgadgets.compose.R
import com.zedalpha.shadowgadgets.core.blendShadowColors
import com.zedalpha.shadowgadgets.core.layer.LocationTracker
import com.zedalpha.shadowgadgets.core.resolveThemeShadowAlphas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@Composable
internal fun blend(
    ambientColor: Color,
    spotColor: Color
): Color {
    val context = LocalContext.current
    val (ambientAlpha, spotAlpha) =
        remember(LocalConfiguration.current) {
            context.resolveThemeShadowAlphas()
        }
    return remember(ambientColor, ambientAlpha, spotColor, spotAlpha) {
        val ambient = ambientColor.toArgb()
        val spot = spotColor.toArgb()
        Color(blendShadowColors(ambient, ambientAlpha, spot, spotAlpha))
    }
}

internal val View.screenLocation: SharedFlow<IntOffset>
    get() = (locationDispatcher ?: LocationDispatcher(this)).screenLocation

private inline var View.locationDispatcher: LocationDispatcher?
    get() = getTag(R.id.location_dispatcher) as? LocationDispatcher
    set(value) = setTag(R.id.location_dispatcher, value)

private class LocationDispatcher(private val view: View) {

    private val _screenLocation = MutableSharedFlow<IntOffset>(replay = 1)
    val screenLocation = _screenLocation.asSharedFlow()

    private val tracker = LocationTracker(view).apply { initialize() }

    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = addPreDrawListener()
        override fun onViewDetachedFromWindow(v: View) = removePreDrawListener()
    }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        with(tracker) {
            if (!checkLocationChanged()) return@with
            val offset = IntOffset(current[0], current[1])
            _screenLocation.tryEmit(offset)
        }
        true
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        view.locationDispatcher = this
        view.addOnAttachStateChangeListener(attachListener)
        if (view.isAttachedToWindow) addPreDrawListener()

        _screenLocation.subscriptionCount
            .map { count -> count > 0 }
            .distinctUntilChanged()
            .onEach { isActive -> if (!isActive) dispose() }
            .launchIn(scope)
    }

    private fun dispose() {
        view.locationDispatcher = null
        view.removeOnAttachStateChangeListener(attachListener)
        removePreDrawListener()
        scope.cancel()
    }

    private var viewTreeObserver: ViewTreeObserver? = null

    private fun addPreDrawListener() {
        viewTreeObserver = view.viewTreeObserver.apply {
            if (isAlive) addOnPreDrawListener(preDrawListener)
        }
    }

    private fun removePreDrawListener() {
        val observer = viewTreeObserver ?: return
        if (observer.isAlive) observer.removeOnPreDrawListener(preDrawListener)
        viewTreeObserver = null
    }
}