package org.sysarp.project.utils

import kotlinx.cinterop.*
import platform.CommonCrypto.*
import platform.Foundation.*

/**
 * Implementación iOS de SHA-256 usando CommonCrypto
 */
actual fun sha256Hash(input: String): String {
    val data = input.encodeToByteArray()
    val hash = ByteArray(CC_SHA256_DIGEST_LENGTH.toInt())
    
    data.usePinned { pinnedData ->
        hash.usePinned { pinnedHash ->
            CC_SHA256(
                pinnedData.addressOf(0),
                data.size.convert(),
                pinnedHash.addressOf(0)
            )
        }
    }
    
    return hash.joinToString("") { "%02x".format(it) }
}
