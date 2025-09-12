package org.sysarp.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform