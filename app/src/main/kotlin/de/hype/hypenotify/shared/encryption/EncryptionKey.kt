package de.hype.hypenotify.shared.encryption

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.Serializable
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.UUID
import javax.crypto.Cipher
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

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
        if (privateKey == null) {
            if (privateKeyEncoded != null) {
                try {
                    val spec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyEncoded))
                    val keyFactory = KeyFactory.getInstance("RSA")
                    privateKey = keyFactory.generatePrivate(spec)
                } catch (e: Exception) {
                    throw RuntimeException("Error decoding private key", e)
                }
            } else {
                // Try to resolve from Android Keystore using id as alias
                try {
                    val ks = KeyStore.getInstance(ANDROID_KEYSTORE)
                    ks.load(null)
                    val entry = ks.getEntry(id, null) as? KeyStore.PrivateKeyEntry
                    privateKey = entry?.privateKey
                } catch (_: Exception) {
                    // ignore, will return null
                }
            }
        }
        return privateKey
    }

    fun hasPrivateKey(): Boolean {
        return privateKeyEncoded != null || run {
            try {
                val ks = KeyStore.getInstance(ANDROID_KEYSTORE)
                ks.load(null)
                ks.containsAlias(id)
            } catch (_: Exception) { false }
        }
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
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MAIN_KEY_ALIAS = "HypeNotifyMainRSAKey"

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

        // Create or return the main key that is stored in Android Keystore
        fun getMainKey(): EncryptionKey? {
            return try {
                val ks = KeyStore.getInstance(ANDROID_KEYSTORE)
                ks.load(null)
                if (!ks.containsAlias(MAIN_KEY_ALIAS)) {
                    generateAndStoreMainKey("Main")
                }
                val cert = ks.getCertificate(MAIN_KEY_ALIAS) ?: return null
                val pub = cert.publicKey
                val pubEncoded = Base64.getEncoder().encodeToString(pub.encoded)
                EncryptionKey(MAIN_KEY_ALIAS, "Main", pubEncoded, null)
            } catch (e: Exception) {
                throw RuntimeException("Error getting main key from Android Keystore", e)
            }
        }

        private fun generateAndStoreMainKey(name: String? = null): EncryptionKey {
            try {
                val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
                val spec = KeyGenParameterSpec.Builder(
                    MAIN_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeySize(4096)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .build()
                kpg.initialize(spec)
                val kp = kpg.generateKeyPair()
                val pubEncoded = Base64.getEncoder().encodeToString(kp.public.encoded)
                return EncryptionKey(MAIN_KEY_ALIAS, name ?: "Main", pubEncoded, null)
            } catch (e: Exception) {
                throw RuntimeException("Error generating Android Keystore main key", e)
            }
        }
    }
}

fun String.encryptWithKey(key: EncryptionKey): String {
    val publicKey = key.getPublicKey()
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val encryptedBytes = cipher.doFinal(this.toByteArray())
    return Base64.getEncoder().encodeToString(encryptedBytes)
}

fun String.decryptWithPrivateKey(): String {
    val pk: PrivateKey = EncryptionKey.getMainKey()?.getPrivateKey()
        ?: throw RuntimeException("Main Key does not exist or is malformed")
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.DECRYPT_MODE, pk)
    val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(this))
    return String(decryptedBytes)
}