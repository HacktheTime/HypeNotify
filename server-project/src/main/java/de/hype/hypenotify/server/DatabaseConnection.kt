package de.hype.hypenotify.server

import java.sql.DriverManager
import java.sql.SQLException

object DatabaseConnection {
    private const val URL = "jdbc:mysql://localhost:3307/"
    private val NOTIFYURL = URL + "notify"
    private const val USER = "bot"
    private const val PASSWORD = "Gdft46uihjcxsEfU"

    @Throws(SQLException::class)
    fun runInDB(code: (java.sql.Connection) -> Unit) {
        var connection: java.sql.Connection? = null
        try {
            connection = DriverManager.getConnection(
                NOTIFYURL,
                USER,
                PASSWORD
            )
            code.invoke(connection)
            connection.close()
        } catch (e: Throwable) {
            try {
                connection?.close()
            }catch (_: Throwable){
                // ignore
            }
        }
    }
}