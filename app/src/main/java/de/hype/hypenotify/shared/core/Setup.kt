package de.hype.hypenotify.shared.core

import de.hype.hypenotify.shared.encryption.EncryptionKey

class Setup {
    init {
        val mainKey = EncryptionKey.Companion.getMainKey()
    }
}