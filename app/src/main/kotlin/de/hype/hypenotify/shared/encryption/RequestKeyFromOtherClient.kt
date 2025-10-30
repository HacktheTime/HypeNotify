package de.hype.hypenotify.shared.encryption

import java.util.Base64
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Repräsentiert eine Anfrage zum Schlüsselaustausch.
 */
class KeyExchangeRequest(
    // Öffentlicher Schlüssel des anfragenden Clients
    val requesterPublicKey: EncryptionKey,
    // Verschlüsselte Session-ID mit dem clientSecret
    val encryptedSessionId: String,
    // Zeitstempel zur Vermeidung von Replay-Attacken
    val timestamp: Long = System.currentTimeMillis(),
    // Eindeutige Request-ID
    val requestId: String = UUID.randomUUID().toString()
)

/**
 * Antwort auf eine Schlüsselaustausch-Anfrage.
 */
class KeyExchangeResponse(
    // Öffentlicher Schlüssel des antwortenden Clients
    val responderPublicKey: EncryptionKey,
    // Bestätigung der Session-ID (mit dem eigenen öffentlichen Schlüssel des Empfängers verschlüsselt)
    val encryptedConfirmation: String,
    // Die ursprüngliche Request-ID
    val originalRequestId: String
)

/**
 * Hilfsfunktionen für den Schlüsselaustausch.
 */
object KeyExchangeUtils {
    // Erzeugt einen abgeleiteten Schlüssel aus dem gemeinsamen Geheimnis
    fun deriveKeyFromSecret(secret: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(secret.toCharArray(), salt, 65536, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    // Verschlüsselt eine Session-ID mit dem abgeleiteten Schlüssel
    fun encryptWithSecret(sessionId: String, secret: String): String {
        val salt = "HypeNotifyExchange".toByteArray()
        val key = deriveKeyFromSecret(secret, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(sessionId.toByteArray())
        val iv = cipher.iv

        // IV und verschlüsselte Daten kombinieren
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.getEncoder().encodeToString(combined)
    }
}

/**
 * Client-seitige Implementierung des Schlüsselaustauschs.
 */
class KeyExchangeProtocol {
    /**
     * Erstellt eine neue Schlüsselaustausch-Anfrage.
     */
    fun createExchangeRequest(myKey: EncryptionKey, clientSecret: String): KeyExchangeRequest {
        val sessionId = UUID.randomUUID().toString()
        val encryptedSessionId = KeyExchangeUtils.encryptWithSecret(sessionId, clientSecret)
        return KeyExchangeRequest(myKey, encryptedSessionId)
    }

    /**
     * Verarbeitet eine eingehende Schlüsselaustausch-Anfrage und erstellt eine Antwort.
     */
    fun processExchangeRequest(request: KeyExchangeRequest, myKey: EncryptionKey, clientSecret: String): KeyExchangeResponse {
        // Implementierung zur Verarbeitung und Validierung der Anfrage
        // ...

        // Bestätigung mit dem öffentlichen Schlüssel des Anfragenden verschlüsseln
        val confirmation = "CONFIRMED-${request.requestId}"
        val encryptedConfirmation = confirmation.encryptWithKey(request.requesterPublicKey)

        return KeyExchangeResponse(myKey, encryptedConfirmation, request.requestId)
    }

    // Extension-Funktion für String zum Verschlüsseln mit einem öffentlichen Schlüssel
    fun String.encryptWithKey(key: EncryptionKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key.getPublicKey())
        val encryptedBytes = cipher.doFinal(this.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }
}