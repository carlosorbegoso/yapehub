package org.sysarp.project

import android.content.Context

object ContextProvider {
    private var _context: Context? = null
    
    fun setContext(context: Context) {
        _context = context
    }
    
    fun getContext(): Context? = _context
}
