package de.hype.hypenotify.shared.data

import de.hype.hypenotify.shared.encryption.EncryptionKey

class Client(
    val firebaseKey: String,
    var encryptionKey: EncryptionKey,
    val id: String,
    val name: String,
){
    @Suppress("unused")
    companion object
}