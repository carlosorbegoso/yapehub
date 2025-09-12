package org.sysarp.project

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppLifecycleManager : DefaultLifecycleObserver {
    private val _appResumed = MutableSharedFlow<Unit>()
    val appResumed: SharedFlow<Unit> = _appResumed.asSharedFlow()
    
    fun initialize(application: Application) {
        // Por ahora solo registramos que se inicializó
        android.util.Log.d("AppLifecycleManager", "Lifecycle manager initialized")
    }
    
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        android.util.Log.d("AppLifecycleManager", "App resumed - emitting event")
        _appResumed.tryEmit(Unit)
    }
}
