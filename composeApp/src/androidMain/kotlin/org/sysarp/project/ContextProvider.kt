package org.sysarp.project

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object ContextProvider {
    private var _context: Context? = null
    
    fun setContext(context: Context) {
        _context = context
    }
    
    fun getContext(): Context? = _context
}
