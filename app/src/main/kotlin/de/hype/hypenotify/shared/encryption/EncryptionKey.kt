package de.hype.hypenotify.shared.encryption

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.Serializable
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.UUID
import javax.crypto.Cipher

class EncryptionKey(
    val id: String,
    var name: String?,
    val publicKeyEncoded: String,
    private val privateKeyEncoded: String? = null
) : Serializable {

    private var publicKey: PublicKey? = null
    private var privateKey: PrivateKey? = null

    constructor(id: String, name: String?, keyPair: KeyPair) : this(
        id,
        name,
        Base64.getEncoder().encodeToString(keyPair.public.encoded),
        Base64.getEncoder().encodeToString(keyPair.private.encoded)
    ) {
        this.publicKey = keyPair.public
        this.privateKey = keyPair.private
    }

    fun getPublicKey(): PublicKey {
        if (publicKey == null && publicKeyEncoded.isNotEmpty()) {
            try {
                val spec = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyEncoded))
                val keyFactory = KeyFactory.getInstance("RSA")
                publicKey = keyFactory.generatePublic(spec)
            } catch (e: Exception) {
                throw RuntimeException("Error decoding public key", e)
            }
        }
        return publicKey!!
    }

    fun getPrivateKey(): PrivateKey? {
        if (privateKey == null && privateKeyEncoded != null) {
            try {
                val spec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyEncoded))
                val keyFactory = KeyFactory.getInstance("RSA")
                privateKey = keyFactory.generatePrivate(spec)
            } catch (e: Exception) {
                throw RuntimeException("Error decoding private key", e)
            }
        }
        return privateKey
    }

    fun hasPrivateKey(): Boolean {
        return privateKeyEncoded != null
    }

    fun toJsonWithPrivateKey(): JsonElement {
        val json = JsonObject()
        json.addProperty("id", id)
        json.addProperty("name", name)
        json.addProperty("publicKey", publicKeyEncoded)
        if (privateKeyEncoded != null) {
            json.addProperty("privateKey", privateKeyEncoded)
        }
        return json
    }

    companion object {
        fun generateRandomKey(name: String? = null): EncryptionKey {
            try {
                val generator = KeyPairGenerator.getInstance("RSA")
                generator.initialize(8192)
                val keyPair = generator.generateKeyPair()
                val actualName = name ?: "Key-${UUID.randomUUID().toString().substring(0, 8)}"
                return EncryptionKey(UUID.randomUUID().toString(), actualName, keyPair)
            } catch (e: Exception) {
                throw RuntimeException("Error generating key pair", e)
            }
        }

    }
}

fun String.encryptWithKey(key: EncryptionKey): String {
    val publicKey = key.getPublicKey()
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val encryptedBytes = cipher.doFinal(this.toByteArray())
    return Base64.getEncoder().encodeToString(encryptedBytes)
}

fun String.decryptWithPrivateKey(): String {
    val privateKey = EncryptionKey.Companion.getMainKey()?.getPrivateKey()
    if (privateKey == null) {
        throw RuntimeException("Main Key does not exist or is malformed")
    }
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(this))
    return String(decryptedBytes)
}