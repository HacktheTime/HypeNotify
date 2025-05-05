package de.hype.hypenotify.server

import de.hype.hypenotify.shared.EncryptionKey
import de.hype.hypenotify.shared.data.Client

public fun Client.save() {
    val client = this
    DatabaseConnection.runInDB { connection ->
        connection.prepareStatement(
            """
            INSERT INTO clients (id, name, public_key, firebase_key) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = ?, public_key = ?, firebase_key = ?
        """.trimIndent()
        )
            .apply {
                setString(1, client.id)
                setString(2, client.name)
                setString(3, client.encryptionKey.publicKeyEncoded)
                setString(4, client.firebaseKey)
                setString(5, client.name)
                setString(6, client.encryptionKey.publicKeyEncoded)
                setString(7, client.firebaseKey)
            }
            .executeUpdate()
    }
}
public fun Client.Companion.load(id: String): Client? {
    var client = null
    DatabaseConnection.runInDB { connection ->
        connection.prepareStatement("""
            SELECT * FROM clients WHERE id = ?
        """.trimIndent()).apply {
            setString(1, id)
        }.executeQuery().apply {
            client = Client(getString("firebase_key"), EncryptionKey.get, getString("id"), getString("name"))
        }
    }
    return client
}