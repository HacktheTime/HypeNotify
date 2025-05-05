package de.hype.hypenotify.server

import de.hype.hypenotify.shared.data.Client

public fun Client.save() {
    val client = this
    DatabaseConnection.runInDB { connection ->
        connection.prepareStatement(
            """
            INSERT INTO clients (id, name, publicKey, firebase_key) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = ?, publicKey = ?, firebase_key = ?;)
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