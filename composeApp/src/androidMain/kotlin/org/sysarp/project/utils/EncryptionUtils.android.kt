package org.sysarp.project.utils

import java.security.MessageDigest

/**
 * Implementación Android de SHA-256 usando MessageDigest
 */
actual fun sha256Hash(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(input.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}
