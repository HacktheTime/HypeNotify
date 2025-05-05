package de.hype.hypenotify.environment

import com.google.gson.GsonBuilder
import de.hype.hypenotify.shared.EncryptionKey
import java.io.File
import java.io.FileReader
import java.io.FileWriter


private const val MAIN_KEY_FILE = "main_encryption_key.json"
private const val PUBLIC_KEYS_DIRECTORY = "public_keys"
private val gson = GsonBuilder().setPrettyPrinting().create()

private val publicKeysById = HashMap<String, EncryptionKey>()
private val publicKeysByName = HashMap<String, EncryptionKey>()


private fun EncryptionKey.Companion.loadPublicKey(id: String) {
    val file = File(PUBLIC_KEYS_DIRECTORY, "$id.json")
    if (file.exists()) {
        try {
            FileReader(file).use { reader ->
                val key = gson.fromJson(reader, EncryptionKey::class.java)
                publicKeysById[id] = key
                key.name?.let { publicKeysByName[it] = key }
            }
        } catch (_: Exception) {
        }
    }
}

fun EncryptionKey.Companion.getMainKey(): EncryptionKey? {
    val file = File(MAIN_KEY_FILE)
    if (file.exists()) {
        try {
            FileReader(file).use { reader ->
                return gson.fromJson(reader, EncryptionKey::class.java)
            }
        } catch (e: Exception) {
            throw RuntimeException("Error reading main key", e)
        }
    } else {
        // If the main key file does not exist, generate a new key
        val newKey = generateRandomKey()
        newKey.saveAsMainKey()
        return newKey
    }
}

fun EncryptionKey.Companion.getPublicKeyById(id: String): EncryptionKey? {
    if (!publicKeysById.containsKey(id)) {
        loadPublicKey(id)
    }
    return publicKeysById[id]
}

fun EncryptionKey.Companion.getPublicKeyByName(name: String): EncryptionKey? {
    if (!publicKeysByName.containsKey(name)) {
        // Search directory for the name
        val directory = File(PUBLIC_KEYS_DIRECTORY)
        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles { _, fileName -> fileName.endsWith(".json") }
            files?.forEach { file ->
                try {
                    FileReader(file).use { reader ->
                        val key = gson.fromJson(reader, EncryptionKey::class.java)
                        if (key.name == name) {
                            publicKeysByName[name] = key
                            publicKeysById[key.id] = key
                            return key
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
    return publicKeysByName[name]
}


fun EncryptionKey.saveAsMainKey() {
    try {
        val file = File(MAIN_KEY_FILE)
        file.parentFile?.mkdirs()
        FileWriter(file).use { writer ->
            gson.toJson(this, writer)
        }
    } catch (e: Exception) {
        throw RuntimeException("Error saving main key", e)
    }
}

fun EncryptionKey.saveAsPublicKey() {
    try {
        val directory = File(PUBLIC_KEYS_DIRECTORY)
        directory.mkdirs()
        val file = File(directory, "$id.json")

        // Create a copy without private key
        val publicOnlyCopy = EncryptionKey(id, name, publicKeyEncoded)

        FileWriter(file).use { writer ->
            gson.toJson(publicOnlyCopy, writer)
        }

        // Update cache
        publicKeysById[id] = publicOnlyCopy
        name?.let { publicKeysByName[it] = publicOnlyCopy }

    } catch (e: Exception) {
        throw RuntimeException("Error saving public key", e)
    }
}